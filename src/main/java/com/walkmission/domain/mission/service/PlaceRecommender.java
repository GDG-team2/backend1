package com.walkmission.domain.mission.service;

import com.walkmission.domain.mission.entity.MoveType;
import com.walkmission.domain.mission.entity.PlaceCategory;
import com.walkmission.global.error.BusinessException;
import com.walkmission.global.error.ErrorCode;
import com.walkmission.global.external.kakao.KakaoLocalClient;
import com.walkmission.global.external.kakao.KakaoPlace;
import com.walkmission.global.util.GeoUtils;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

/**
 * 전체 외출 시간과 이동수단으로 추천 거리 범위를 정하고, 그 범위 안의 장소를 카카오에서 찾아 무작위로 고른다.
 *
 * <p>거리 계산 (범주마다 다름):
 * 이동 시간 = 전체 외출 시간 - 장소에서 보내는 시간, 편도 = 이동 시간 / 2,
 * 실제 이동 거리 = 편도 × 도보 속도 × 이동수단 가중치 × 거리 보정(기분·거절 이유),
 * 이를 우회 계수로 나눠 직선거리로 바꾸고, 최대 거리의 60~100% 구간을 추천 범위로 쓴다.
 */
@Component
public class PlaceRecommender {
    public static final double WALK_METERS_PER_MINUTE = 67;   // 약 4km/h
    public static final double DETOUR_FACTOR = 1.3;           // 실제 이동 거리 ≒ 직선거리 × 1.3 (도시 평균)
    /** 왕복 이동에 최소한 이만큼은 써야 외출이 된다. 이보다 적게 남는 범주는 1차 후보에서 뺀다. */
    private static final int MIN_TRAVEL_MINUTES = 10;
    private static final double MIN_DISTANCE_RATIO = 0.6;
    private static final int DIRECTION_ATTEMPTS = 4;
    private static final double FALLBACK_RADIUS_RATIO = 1.5;
    private static final double FALLBACK_MIN_DISTANCE_RATIO = 0.3;
    private static final int MIN_SEARCH_RADIUS_METERS = 100;

    private final KakaoLocalClient kakaoLocalClient;

    public PlaceRecommender(KakaoLocalClient kakaoLocalClient) {
        this.kakaoLocalClient = kakaoLocalClient;
    }

    public record Candidate(KakaoPlace place, PlaceCategory category, int distanceMeters) {}

    /**
     * @param categoryOrder  시도할 범주 순서 (우선 범주가 앞)
     * @param distanceFactor 거리 보정 배율 (기분, 거절·포기 이유)
     */
    public record Request(double latitude, double longitude, int walkTimeMinutes, MoveType moveType,
                          List<PlaceCategory> categoryOrder, double distanceFactor, Set<String> excludedKakaoIds) {}

    /** 범주별 카카오 검색 방법과 결과 필터 */
    private record SearchSpec(List<String> categoryCodes, List<String> keywords, Predicate<String> categoryNameFilter) {}

    private static final Map<PlaceCategory, SearchSpec> SEARCH_SPECS = new EnumMap<>(Map.of(
            // "공원" 키워드는 공원 주차장도 걸리므로 분류명에 '공원'이 있는 것만
            PlaceCategory.WALK, new SearchSpec(List.of(), List.of("공원"), name -> name.contains("공원")),
            PlaceCategory.CAFE, new SearchSpec(List.of("CE7"), List.of(), name -> !name.contains("키즈카페")),
            PlaceCategory.SIGHTSEEING, new SearchSpec(List.of("AT4"), List.of(), name -> true),
            // 문화시설(CT1)에는 영화관/공연장이 섞여 있어 키워드로 찾고, 미술학원 등은 분류명으로 거른다
            PlaceCategory.EXHIBITION, new SearchSpec(List.of(), List.of("미술관", "갤러리", "박물관"),
                    name -> name.startsWith("문화,예술")),
            PlaceCategory.FOOD, new SearchSpec(List.of("FD6"), List.of(), name -> true)
    ));

    /** 전체 외출 시간 중 왕복 이동에 쓸 수 있는 시간(분) */
    public static int travelMinutes(int walkTimeMinutes, PlaceCategory category) {
        return walkTimeMinutes - category.getStayMinutes();
    }

    /** 추천 범위의 최대 직선거리(미터) */
    public static double maxStraightDistance(int travelMinutes, MoveType moveType, double distanceFactor) {
        double oneWayMinutes = travelMinutes / 2.0;
        double travelMeters = oneWayMinutes * WALK_METERS_PER_MINUTE * moveType.getSpeedWeight() * distanceFactor;
        return travelMeters / DETOUR_FACTOR;
    }

    /** 직선거리 기준 예상 편도 소요 시간(분) */
    public static int oneWayMinutes(int straightDistanceMeters, MoveType moveType) {
        return (int) Math.ceil(straightDistanceMeters * DETOUR_FACTOR / (WALK_METERS_PER_MINUTE * moveType.getSpeedWeight()));
    }

    /** 왕복 이동 + 장소에서 보내는 시간(분) */
    public static int totalMinutes(int straightDistanceMeters, MoveType moveType, PlaceCategory category) {
        return oneWayMinutes(straightDistanceMeters, moveType) * 2 + category.getStayMinutes();
    }

    /** 실제 길 기준 예상 왕복 이동 거리(미터) */
    public static int roundTripRouteMeters(int straightDistanceMeters) {
        return (int) Math.round(straightDistanceMeters * DETOUR_FACTOR * 2);
    }

    public Candidate recommend(Request request) {
        double lat = request.latitude();
        double lon = request.longitude();

        // 장소에서 보낼 시간을 빼고도 이동 시간이 남는 범주만 1차 후보. 하나도 없으면 최소 이동 시간으로 전부 시도한다.
        List<PlaceCategory> fitting = request.categoryOrder().stream()
                .filter(c -> travelMinutes(request.walkTimeMinutes(), c) >= MIN_TRAVEL_MINUTES)
                .toList();
        List<PlaceCategory> categories = fitting.isEmpty() ? request.categoryOrder() : fitting;

        // 1차: 무작위 방향으로 목표 거리만큼 떨어진 지점 주변을 검색한다.
        //      밀집 지역에서 "가까운 순 15개"만 받으면 너무 가까운 곳만 나오는 문제를 피하기 위함.
        double startBearing = ThreadLocalRandom.current().nextDouble(360);
        for (PlaceCategory category : categories) {
            double maxDistance = maxDistanceFor(request, category);
            double minDistance = maxDistance * MIN_DISTANCE_RATIO;
            double searchCenterDistance = (minDistance + maxDistance) / 2;
            int searchRadius = (int) Math.max(MIN_SEARCH_RADIUS_METERS, (maxDistance - minDistance) * 0.6);

            for (int attempt = 0; attempt < DIRECTION_ATTEMPTS; attempt++) {
                double[] center = GeoUtils.destination(lat, lon,
                        startBearing + attempt * (360.0 / DIRECTION_ATTEMPTS), searchCenterDistance);
                List<Candidate> candidates = search(category, center[0], center[1], searchRadius,
                        lat, lon, minDistance, maxDistance, request.excludedKakaoIds());
                if (!candidates.isEmpty()) return pickRandom(candidates);
            }
        }

        // 2차: 범위를 넓혀서 찾되, 너무 가까운 곳은 산책이 되지 않으므로 최소 거리를 둔다.
        //      그래도 없으면(장소가 드문 범주) 가까운 곳까지 허용한다.
        List<Candidate> nearbyOnly = new ArrayList<>();
        for (PlaceCategory category : categories) {
            double maxDistance = maxDistanceFor(request, category);
            double fallbackMax = maxDistance * FALLBACK_RADIUS_RATIO;
            double fallbackMin = maxDistance * FALLBACK_MIN_DISTANCE_RATIO;
            List<Candidate> candidates = search(category, lat, lon, (int) Math.ceil(fallbackMax),
                    lat, lon, 0, fallbackMax, request.excludedKakaoIds());
            List<Candidate> farEnough = candidates.stream().filter(c -> c.distanceMeters() >= fallbackMin).toList();
            if (!farEnough.isEmpty()) return pickRandom(farEnough);
            nearbyOnly.addAll(candidates);
        }
        if (!nearbyOnly.isEmpty()) return pickRandom(nearbyOnly);

        throw new BusinessException(ErrorCode.NO_NEARBY_PLACE);
    }

    private double maxDistanceFor(Request request, PlaceCategory category) {
        int travel = Math.max(MIN_TRAVEL_MINUTES, travelMinutes(request.walkTimeMinutes(), category));
        return maxStraightDistance(travel, request.moveType(), request.distanceFactor());
    }

    private List<Candidate> search(PlaceCategory category, double centerLat, double centerLon, int radius,
                                   double userLat, double userLon, double minDistance, double maxDistance,
                                   Set<String> excludedKakaoIds) {
        SearchSpec spec = SEARCH_SPECS.get(category);
        List<KakaoPlace> results = new ArrayList<>();
        spec.categoryCodes().forEach(code ->
                results.addAll(kakaoLocalClient.searchByCategory(code, centerLat, centerLon, radius)));
        spec.keywords().forEach(keyword ->
                results.addAll(kakaoLocalClient.searchByKeyword(keyword, centerLat, centerLon, radius)));

        Map<String, Candidate> candidates = new LinkedHashMap<>();
        for (KakaoPlace place : results) {
            if (place.id() == null || excludedKakaoIds.contains(place.id())) continue;
            if (place.categoryName() == null || !spec.categoryNameFilter().test(place.categoryName())) continue;

            double distance = GeoUtils.distanceMeters(userLat, userLon,
                    Double.parseDouble(place.y()), Double.parseDouble(place.x()));
            if (distance < minDistance || distance > maxDistance) continue;

            candidates.putIfAbsent(place.id(), new Candidate(place, category, (int) Math.round(distance)));
        }
        return new ArrayList<>(candidates.values());
    }

    private Candidate pickRandom(List<Candidate> candidates) {
        return candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
    }
}
