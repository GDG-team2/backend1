package com.walkmission.domain.mission.service;

import com.walkmission.domain.mission.dto.*;
import com.walkmission.domain.mission.entity.*;
import com.walkmission.domain.mission.repository.MissionRecordRepository;
import com.walkmission.domain.mission.repository.PlaceRepository;
import com.walkmission.domain.ranking.service.RankingService;
import com.walkmission.domain.reward.RewardPolicy;
import com.walkmission.domain.reward.entity.Badge;
import com.walkmission.domain.reward.service.RewardService;
import com.walkmission.domain.user.entity.User;
import com.walkmission.domain.user.entity.UserMissionPreference;
import com.walkmission.domain.user.repository.UserMissionPreferenceRepository;
import com.walkmission.domain.user.repository.UserRepository;
import com.walkmission.domain.user.service.RhythmService;
import com.walkmission.global.error.BusinessException;
import com.walkmission.global.error.ErrorCode;
import com.walkmission.global.external.kakao.KakaoPlace;
import com.walkmission.global.util.GeoUtils;
import com.walkmission.global.util.TimeUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class MissionService {
    private static final double ARRIVAL_RADIUS_METERS = 80;
    private static final int ARRIVAL_DWELL_SECONDS = 30;
    private static final int RECENT_PLACE_EXCLUDE_DAYS = 7;
    private static final int MAX_SCHEDULE_DAYS = 14;
    /** "너무 멀어요"로 거절했을 때 거리 배율 */
    private static final double TOO_FAR_DISTANCE_FACTOR = 0.7;
    /** 같은 날 "멀었어요/피곤해요"로 포기했을 때 거리 배율 */
    private static final double AFTER_TIRING_ABORT_DISTANCE_FACTOR = 0.8;

    private final MissionRecordRepository missionRecordRepository;
    private final PlaceRepository placeRepository;
    private final UserRepository userRepository;
    private final RewardService rewardService;
    private final RankingService rankingService;
    private final UserMissionPreferenceRepository preferenceRepository;
    private final PlaceRecommender placeRecommender;
    private final RhythmService rhythmService;
    private final MissionTextWriter missionTextWriter;

    public MissionService(MissionRecordRepository missionRecordRepository, PlaceRepository placeRepository,
                          UserRepository userRepository, RewardService rewardService,
                          RankingService rankingService, UserMissionPreferenceRepository preferenceRepository,
                          PlaceRecommender placeRecommender, RhythmService rhythmService,
                          MissionTextWriter missionTextWriter) {
        this.missionRecordRepository = missionRecordRepository;
        this.placeRepository = placeRepository;
        this.userRepository = userRepository;
        this.rewardService = rewardService;
        this.rankingService = rankingService;
        this.preferenceRepository = preferenceRepository;
        this.placeRecommender = placeRecommender;
        this.rhythmService = rhythmService;
        this.missionTextWriter = missionTextWriter;
    }

    /**
     * 미션 추천. 요청 값 > 선호 설정 > 기본값 순으로 조건을 정하고,
     * 기분·예산·거절 이유·오늘의 포기 이유를 거리와 범주에 반영한다.
     */
    @Transactional
    public MissionRecommendResponse recommend(Long userId, MissionRecommendRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (missionRecordRepository.existsByUserIdAndStatusIn(userId, MissionStatus.WALKING)) {
            throw new BusinessException(ErrorCode.ACTIVE_MISSION_EXISTS);
        }

        UserMissionPreference preference = preferenceRepository.findByUserId(userId).orElse(null);
        int walkTime = preference != null ? preference.getWalkTime() : UserMissionPreference.DEFAULT_WALK_TIME;
        MoveType moveType = request.moveType() != null ? request.moveType()
                : preference != null ? preference.getMoveType() : MoveType.WALK;
        Budget budget = request.budget() != null ? request.budget()
                : preference != null ? preference.getBudget() : Budget.ANY;
        Set<PlaceCategory> categories = EnumSet.copyOf(request.category() != null ? EnumSet.of(request.category())
                : preference != null ? preference.getEffectiveCategories() : EnumSet.allOf(PlaceCategory.class));
        Mood mood = request.mood();
        RejectReason rejectReason = request.rejectReason();
        double distanceFactor = mood != null ? mood.getDistanceFactor() : 1.0;

        // 최근 완료한 곳은 제외
        LocalDateTime now = LocalDateTime.now();
        Set<String> excludedKakaoIds = new HashSet<>(missionRecordRepository.findPlaceKakaoIdsCompletedSince(
                userId, MissionStatus.COMPLETED, now.minusDays(RECENT_PLACE_EXCLUDE_DAYS)));

        // 다시 추천: 출발을 약속하지 않은 추천(READY)을 대체하고 그 장소는 제외, 거절 이유를 이번 추천에 반영.
        //           출발을 약속한 예정 미션은 그대로 둔다.
        PlaceCategory rejectedCategory = null;
        for (MissionRecord previous : missionRecordRepository.findByUserIdAndStatusAndScheduledAtIsNull(userId, MissionStatus.READY)) {
            excludedKakaoIds.add(previous.getPlace().getKakaoPlaceId());
            rejectedCategory = previous.getPlaceCategory();
            previous.replaceByNewRecommendation(rejectReason, now);
        }
        if (rejectReason == RejectReason.TOO_FAR) distanceFactor *= TOO_FAR_DISTANCE_FACTOR;
        if (rejectReason == RejectReason.NO_SPENDING) budget = Budget.FREE;
        if (rejectReason == RejectReason.DISLIKE_ACTIVITY && rejectedCategory != null && categories.size() > 1) {
            categories.remove(rejectedCategory);
        }

        // 오늘 "멀었어요/피곤해요"로 멈춘 적이 있으면 더 가까운 곳부터
        if (missionRecordRepository.existsByUserIdAndStatusAndAbortReasonInAndAbortedAtGreaterThanEqual(
                userId, MissionStatus.ABORTED, shorteningAbortReasons(), TimeUtils.today().atStartOfDay())) {
            distanceFactor *= AFTER_TIRING_ABORT_DISTANCE_FACTOR;
        }

        // 예산을 넘는 범주는 제외
        Budget effectiveBudget = budget;
        categories.removeIf(c -> !effectiveBudget.allows(c.getEstimatedCost()));
        if (categories.isEmpty()) {
            throw new BusinessException(ErrorCode.NO_NEARBY_PLACE, Map.of("reason", "BUDGET"));
        }

        // 기분이 선호하는 범주를 먼저 시도
        List<PlaceCategory> categoryOrder = orderCategories(categories, mood);

        // 새로운 곳을 원하면(심심함·꿀꿀함, "이미 가본 곳이에요") 가본 적 없는 곳만. 없으면 조건을 풀어서 다시 찾는다.
        boolean onlyNewPlaces = rejectReason == RejectReason.ALREADY_VISITED || (mood != null && mood.prefersNewPlace());
        PlaceRecommender.Candidate candidate;
        if (onlyNewPlaces) {
            Set<String> withVisited = new HashSet<>(excludedKakaoIds);
            withVisited.addAll(missionRecordRepository.findAllCompletedPlaceKakaoIds(userId));
            try {
                candidate = placeRecommender.recommend(recommendRequest(request, walkTime, moveType, categoryOrder,
                        distanceFactor, withVisited));
            } catch (BusinessException e) {
                if (e.getErrorCode() != ErrorCode.NO_NEARBY_PLACE) throw e;
                candidate = placeRecommender.recommend(recommendRequest(request, walkTime, moveType, categoryOrder,
                        distanceFactor, excludedKakaoIds));
            }
        } else {
            candidate = placeRecommender.recommend(recommendRequest(request, walkTime, moveType, categoryOrder,
                    distanceFactor, excludedKakaoIds));
        }

        Place place = findOrCreatePlace(candidate.place());
        PlaceCategory category = candidate.category();
        boolean isNewPlace = !missionRecordRepository.existsByUserIdAndPlaceIdAndStatus(
                userId, place.getId(), MissionStatus.COMPLETED);
        String title = missionTextWriter.title(place.getName(), category);
        String reason = missionTextWriter.reason(userId, category, isNewPlace, mood, rejectReason, budget);

        int distance = candidate.distanceMeters();
        MissionRecord mission = missionRecordRepository.save(
                new MissionRecord(user, place, category, isNewPlace, title, reason, mood, moveType, distance));

        return new MissionRecommendResponse(
                mission.getId(),
                mission.getStatus().name(),
                title,
                reason,
                toPlaceInfo(place),
                distance,
                PlaceRecommender.roundTripRouteMeters(distance),
                PlaceRecommender.oneWayMinutes(distance, moveType),
                PlaceRecommender.totalMinutes(distance, moveType, category),
                category.getEstimatedCost(),
                isNewPlace,
                RewardPolicy.MISSION_COMPLETE_POINT,
                category,
                moveType,
                budget
        );
    }

    /** 출발 시각을 약속한다(이미 약속했으면 시간 변경). 예정 미션은 여러 개 가질 수 있다. */
    @Transactional
    public ScheduledMissionInfo schedule(Long userId, Long missionId, MissionScheduleRequest request) {
        MissionRecord mission = getMissionForUpdate(userId, missionId);
        requireStatus(mission, MissionStatus.READY);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime departAt = request.departAt();
        if (departAt.isBefore(now.minusMinutes(1)) || departAt.isAfter(now.plusDays(MAX_SCHEDULE_DAYS))) {
            throw new BusinessException(ErrorCode.INVALID_INPUT,
                    Map.of("departAt", "지금부터 " + MAX_SCHEDULE_DAYS + "일 이내의 시각이어야 합니다"));
        }

        mission.schedule(departAt);
        return toScheduledInfo(mission, now);
    }

    @Transactional(readOnly = true)
    public ScheduledMissionsResponse getScheduled(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        return new ScheduledMissionsResponse(
                missionRecordRepository.findByUserIdAndStatusAndScheduledAtIsNotNullOrderByScheduledAtAsc(
                                userId, MissionStatus.READY).stream()
                        .map(m -> toScheduledInfo(m, now))
                        .toList());
    }

    @Transactional
    public MissionStartResponse start(Long userId, Long missionId) {
        MissionRecord mission = getMissionForUpdate(userId, missionId);
        requireStatus(mission, MissionStatus.READY);

        if (missionRecordRepository.existsByUserIdAndStatusIn(userId, MissionStatus.WALKING)) {
            throw new BusinessException(ErrorCode.ACTIVE_MISSION_EXISTS);
        }

        mission.start(LocalDateTime.now());

        return new MissionStartResponse(
                mission.getId(), mission.getStatus().name(), mission.getStartedAt(),
                "산책 미션을 시작했습니다. 안전하게 이동하세요!");
    }

    /**
     * 도착 인증: 반경 안에서 처음 요청한 시각부터 체류 시간을 재고, 채운 뒤 다시 요청하면 도착으로 인정한다.
     * 반경을 벗어나면 체류 시간을 초기화한다. 체류 시작/초기화는 에러 응답이어도 저장돼야 하므로 롤백하지 않는다.
     */
    @Transactional(noRollbackFor = BusinessException.class)
    public MissionArriveResponse arrive(Long userId, Long missionId, MissionArriveRequest request) {
        MissionRecord mission = getMissionForUpdate(userId, missionId);
        requireStatus(mission, MissionStatus.IN_PROGRESS);

        Place place = mission.getPlace();
        double distance = GeoUtils.distanceMeters(
                request.latitude(), request.longitude(), place.getLatitude(), place.getLongitude());
        if (distance > ARRIVAL_RADIUS_METERS) {
            mission.resetArrivalCheck();
            throw new BusinessException(ErrorCode.NOT_ENOUGH_DISTANCE, Map.of("currentDistanceMeters", (int) Math.round(distance)));
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime checkStartedAt = mission.markInsideArrivalZone(now);
        long dwelled = Duration.between(checkStartedAt, now).getSeconds();
        if (dwelled < ARRIVAL_DWELL_SECONDS) {
            int remaining = (int) (ARRIVAL_DWELL_SECONDS - dwelled);
            return new MissionArriveResponse(mission.getId(), mission.getStatus().name(), null, remaining,
                    "목적지 근처예요. " + remaining + "초만 머물러 주세요.");
        }

        mission.arrive(now);
        return new MissionArriveResponse(mission.getId(), mission.getStatus().name(), mission.getArrivedAt(), 0,
                "목적지에 도착했어요! 주변을 둘러보세요.");
    }

    @Transactional
    public MissionCompleteResponse complete(Long userId, Long missionId, MissionCompleteRequest request) {
        MissionRecord mission = getMissionForUpdate(userId, missionId);
        requireStatus(mission, MissionStatus.ARRIVED);

        mission.complete(request.afterSurveyScore(), request.stepCount(), LocalDateTime.now());

        // 정산 순서: 포인트 → 주간 리듬 → 랭킹 → 배지 (배지 조건이 갱신된 리듬/완료 횟수를 참조)
        User user = mission.getUser();
        int earnedPoint = RewardPolicy.MISSION_COMPLETE_POINT;
        int currentTotalPoint = rewardService.earnPoint(
                user, earnedPoint, "미션 완료 보상 - " + mission.getMissionTitle());
        RhythmService.RhythmResult rhythm = rhythmService.evaluate(user.getId());
        RankingService.ScoreResult score = rankingService.addMissionScore(user, mission);
        List<Badge> newBadges = rewardService.awardBadges(user);

        Integer durationMinutes = mission.getStartedAt() == null ? null
                : (int) Duration.between(mission.getStartedAt(), mission.getCompletedAt()).toMinutes();

        return new MissionCompleteResponse(
                mission.getId(),
                mission.getStatus().name(),
                mission.getCompletedAt(),
                mission.getStepCount(),
                Boolean.TRUE.equals(mission.getIsNewPlace()),
                durationMinutes,
                new MissionCompleteResponse.RewardInfo(earnedPoint, currentTotalPoint),
                new MissionCompleteResponse.RankingInfo(score.isParticipant(), score.earnedScore(), score.bonusScore(),
                        score.weeklyScore(), score.scoredMissionCount(), score.maxScoredMissions()),
                new MissionCompleteResponse.RhythmInfo(rhythm.weeklyGoal(), rhythm.thisWeekCount(),
                        rhythm.goalJustAchieved(), rhythm.currentWeeks()),
                newBadges.stream()
                        .map(b -> new MissionCompleteResponse.BadgeInfo(
                                b.getId(), b.getBadgeName(), b.getDescription(), b.getIconUrl()))
                        .toList()
        );
    }

    @Transactional
    public MissionAbortResponse abort(Long userId, Long missionId, MissionAbortRequest request) {
        MissionRecord mission = getMissionForUpdate(userId, missionId);
        if (!mission.getStatus().isActive()) {
            throw new BusinessException(ErrorCode.INVALID_MISSION_STATUS, Map.of("currentStatus", mission.getStatus().name()));
        }

        AbortReason reason = request != null ? request.reason() : null;
        Integer moved = request != null ? request.movedDistanceMeters() : null;
        mission.abort(reason, moved, LocalDateTime.now());

        String note = reason != null && reason.shortensNextMission() ? "오늘은 더 가까운 곳부터 제안할게요." : null;
        return new MissionAbortResponse(
                mission.getId(), mission.getStatus().name(), mission.getAbortedAt(), moved, note, "멈춰도 기록은 남아요.");
    }

    /** 앱 복구용: 산책 중인 미션 > 약속하지 않은 추천 > 가장 가까운 예정 미션 */
    @Transactional(readOnly = true)
    public CurrentMissionResponse getCurrent(Long userId) {
        Optional<MissionRecord> current = missionRecordRepository
                .findFirstByUserIdAndStatusInOrderByIdDesc(userId, MissionStatus.WALKING)
                .or(() -> missionRecordRepository.findFirstByUserIdAndStatusAndScheduledAtIsNullOrderByIdDesc(
                        userId, MissionStatus.READY))
                .or(() -> missionRecordRepository.findByUserIdAndStatusAndScheduledAtIsNotNullOrderByScheduledAtAsc(
                        userId, MissionStatus.READY).stream().findFirst());
        return current
                .map(mission -> {
                    Place place = mission.getPlace();
                    return new CurrentMissionResponse(true, new CurrentMissionResponse.ActiveMissionInfo(
                            mission.getId(),
                            mission.getStatus().name(),
                            mission.getMissionTitle(),
                            mission.getScheduledAt(),
                            mission.getStartedAt(),
                            new CurrentMissionResponse.PlaceInfo(
                                    place.getId(), place.getKakaoPlaceId(), place.getName(), place.getCategory(),
                                    place.getRoadAddress(), place.getLatitude(), place.getLongitude(),
                                    place.getPlaceUrl()),
                            RewardPolicy.MISSION_COMPLETE_POINT));
                })
                .orElse(new CurrentMissionResponse(false, null));
    }

    private PlaceRecommender.Request recommendRequest(MissionRecommendRequest request, int walkTime, MoveType moveType,
                                                      List<PlaceCategory> categoryOrder, double distanceFactor,
                                                      Set<String> excludedKakaoIds) {
        return new PlaceRecommender.Request(request.latitude().doubleValue(), request.longitude().doubleValue(),
                walkTime, moveType, categoryOrder, distanceFactor, excludedKakaoIds);
    }

    /** 기분이 선호하는 범주를 앞에, 나머지를 뒤에 두고 각각 섞는다. */
    private List<PlaceCategory> orderCategories(Set<PlaceCategory> categories, Mood mood) {
        List<PlaceCategory> preferred = new ArrayList<>();
        List<PlaceCategory> others = new ArrayList<>();
        for (PlaceCategory c : categories) {
            if (mood != null && mood.getPreferredCategories().contains(c)) preferred.add(c);
            else others.add(c);
        }
        Collections.shuffle(preferred);
        Collections.shuffle(others);
        preferred.addAll(others);
        return preferred;
    }

    private static List<AbortReason> shorteningAbortReasons() {
        return Arrays.stream(AbortReason.values()).filter(AbortReason::shortensNextMission).toList();
    }

    private ScheduledMissionInfo toScheduledInfo(MissionRecord mission, LocalDateTime now) {
        LocalDateTime at = mission.getScheduledAt();
        long secondsLeft = Duration.between(now, at).getSeconds();
        long minutesLeft = Math.max(0, (secondsLeft + 59) / 60); // "10분 뒤" 약속이 9분으로 보이지 않도록 올림
        PlaceCategory category = mission.getPlaceCategory();
        Integer distance = mission.getDistanceMeters();
        MoveType moveType = mission.getMoveType();
        return new ScheduledMissionInfo(
                mission.getId(),
                mission.getMissionTitle(),
                at,
                (int) minutesLeft,
                at.isBefore(now),
                mission.getPlaceNameSnapshot(),
                category,
                moveType,
                distance != null ? PlaceRecommender.oneWayMinutes(distance, moveType) : null,
                distance != null && category != null ? PlaceRecommender.totalMinutes(distance, moveType, category) : null,
                category != null ? category.getEstimatedCost() : null);
    }

    private MissionRecommendResponse.PlaceInfo toPlaceInfo(Place place) {
        return new MissionRecommendResponse.PlaceInfo(
                place.getId(), place.getKakaoPlaceId(), place.getName(), place.getCategory(),
                place.getRoadAddress(), place.getLatitude(), place.getLongitude(), place.getPlaceUrl());
    }

    /** 카카오 장소를 Place 테이블에 저장(이미 있으면 재사용)한다. */
    private Place findOrCreatePlace(KakaoPlace kakaoPlace) {
        return placeRepository.findByKakaoPlaceId(kakaoPlace.id())
                .orElseGet(() -> placeRepository.save(new Place(
                        kakaoPlace.id(),
                        kakaoPlace.placeName(),
                        kakaoPlace.address(),
                        kakaoPlace.leafCategory(),
                        new BigDecimal(kakaoPlace.y()),
                        new BigDecimal(kakaoPlace.x()),
                        kakaoPlace.placeUrl())));
    }

    private MissionRecord getMissionForUpdate(Long userId, Long missionId) {
        return missionRecordRepository.findByIdAndUserIdForUpdate(missionId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MISSION_NOT_FOUND));
    }

    private void requireStatus(MissionRecord mission, MissionStatus expected) {
        if (mission.getStatus() != expected) {
            throw new BusinessException(ErrorCode.INVALID_MISSION_STATUS, Map.of("currentStatus", mission.getStatus().name()));
        }
    }
}
