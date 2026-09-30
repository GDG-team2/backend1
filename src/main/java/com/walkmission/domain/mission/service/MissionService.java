package com.walkmission.domain.mission.service;

import com.walkmission.domain.mission.dto.*;
import com.walkmission.domain.mission.entity.MissionRecord;
import com.walkmission.domain.mission.entity.MissionStatus;
import com.walkmission.domain.mission.entity.Place;
import com.walkmission.domain.mission.repository.MissionRecordRepository;
import com.walkmission.domain.mission.repository.PlaceRepository;
import com.walkmission.domain.ranking.service.RankingService;
import com.walkmission.domain.reward.RewardPolicy;
import com.walkmission.domain.reward.entity.Badge;
import com.walkmission.domain.reward.service.RewardService;
import com.walkmission.domain.user.entity.User;
import com.walkmission.domain.user.repository.UserRepository;
import com.walkmission.global.util.GeoUtils;
import com.walkmission.global.error.BusinessException;
import com.walkmission.global.error.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
public class MissionService {
    private static final double ARRIVAL_RADIUS_METERS = 50;
    private static final double SEARCH_RADIUS_METERS = 3_000;
    private static final double METERS_PER_DEGREE_LATITUDE = 111_320;
    private static final double WALK_METERS_PER_MINUTE = 67; // 약 4km/h

    private final MissionRecordRepository missionRecordRepository;
    private final PlaceRepository placeRepository;
    private final UserRepository userRepository;
    private final RewardService rewardService;
    private final RankingService rankingService;

    public MissionService(MissionRecordRepository missionRecordRepository, PlaceRepository placeRepository,
                          UserRepository userRepository, RewardService rewardService,
                          RankingService rankingService) {
        this.missionRecordRepository = missionRecordRepository;
        this.placeRepository = placeRepository;
        this.userRepository = userRepository;
        this.rewardService = rewardService;
        this.rankingService = rankingService;
    }

    @Transactional
    public MissionRecommendResponse recommend(Long userId, MissionRecommendRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (missionRecordRepository.existsByUserIdAndStatusIn(userId, MissionStatus.WALKING)) {
            throw new BusinessException(ErrorCode.ACTIVE_MISSION_EXISTS);
        }

        // 출발 전(READY) 미션은 새 추천으로 대체한다.
        LocalDateTime now = LocalDateTime.now();
        missionRecordRepository.findByUserIdAndStatus(userId, MissionStatus.READY)
                .forEach(mission -> mission.abort(now));

        Place place = findNearestPlace(request.latitude(), request.longitude());
        int distance = (int) Math.round(GeoUtils.distanceMeters(
                request.latitude(), request.longitude(), place.getLatitude(), place.getLongitude()));
        boolean isNewPlace = !missionRecordRepository.existsByUserIdAndPlaceIdAndStatus(
                userId, place.getId(), MissionStatus.COMPLETED);

        MissionRecord mission = missionRecordRepository.save(new MissionRecord(user, place, isNewPlace));

        return new MissionRecommendResponse(
                mission.getId(),
                mission.getStatus().name(),
                new MissionRecommendResponse.PlaceInfo(
                        place.getId(), place.getKakaoPlaceId(), place.getName(), place.getCategory(),
                        place.getRoadAddress(), place.getLatitude(), place.getLongitude()),
                distance,
                (int) Math.ceil(distance / WALK_METERS_PER_MINUTE),
                RewardPolicy.MISSION_COMPLETE_POINT
        );
    }

    @Transactional
    public MissionStartResponse start(Long userId, Long missionId, MissionStartRequest request) {
        MissionRecord mission = getMission(userId, missionId);
        requireStatus(mission, MissionStatus.READY);

        if (missionRecordRepository.existsByUserIdAndStatusIn(userId, MissionStatus.WALKING)) {
            throw new BusinessException(ErrorCode.ACTIVE_MISSION_EXISTS);
        }

        Integer beforeSurvey = request != null ? request.beforeSurveyScore() : null;
        mission.start(beforeSurvey, LocalDateTime.now());

        return new MissionStartResponse(
                mission.getId(), mission.getStatus().name(), mission.getStartedAt(),
                "산책 미션을 시작했습니다. 안전하게 이동하세요!");
    }

    @Transactional
    public MissionArriveResponse arrive(Long userId, Long missionId, MissionArriveRequest request) {
        MissionRecord mission = getMission(userId, missionId);
        requireStatus(mission, MissionStatus.IN_PROGRESS);

        Place place = mission.getPlace();
        double distance = GeoUtils.distanceMeters(
                request.latitude(), request.longitude(), place.getLatitude(), place.getLongitude());
        if (distance > ARRIVAL_RADIUS_METERS) {
            throw new BusinessException(ErrorCode.NOT_ENOUGH_DISTANCE, Map.of("currentDistanceMeters", (int) Math.round(distance)));
        }

        mission.arrive(LocalDateTime.now());

        return new MissionArriveResponse(
                mission.getId(), mission.getStatus().name(), mission.getArrivedAt(),
                "목적지에 도착했습니다! 주변을 둘러보세요.");
    }

    @Transactional
    public MissionCompleteResponse complete(Long userId, Long missionId, MissionCompleteRequest request) {
        MissionRecord mission = getMission(userId, missionId);
        requireStatus(mission, MissionStatus.ARRIVED);

        mission.complete(request.afterSurveyScore(), request.stepCount(), LocalDateTime.now());

        // 정산 순서: 포인트 → 스트릭 → 랭킹 → 배지 (배지 조건이 갱신된 스트릭/완료 횟수를 참조)
        User user = mission.getUser();
        int earnedPoint = RewardPolicy.MISSION_COMPLETE_POINT;
        int currentTotalPoint = rewardService.earnPoint(
                user, earnedPoint, "미션 완료 보상 - " + mission.getPlaceNameSnapshot());
        RewardService.StreakResult streak = rewardService.recordStreak(user.getId());
        RankingService.ScoreResult score = rankingService.addMissionScore(user);
        List<Badge> newBadges = rewardService.awardBadges(user);

        return new MissionCompleteResponse(
                mission.getId(),
                mission.getStatus().name(),
                mission.getCompletedAt(),
                mission.getStepCount(),
                new MissionCompleteResponse.RewardInfo(earnedPoint, currentTotalPoint),
                new MissionCompleteResponse.RankingInfo(score.isParticipant(), score.earnedScore(), score.weeklyScore()),
                new MissionCompleteResponse.StreakInfo(streak.streakNow(), streak.isMaintained()),
                newBadges.stream()
                        .map(b -> new MissionCompleteResponse.BadgeInfo(
                                b.getId(), b.getBadgeName(), b.getDescription(), b.getIconUrl()))
                        .toList()
        );
    }

    @Transactional
    public MissionAbortResponse abort(Long userId, Long missionId) {
        MissionRecord mission = getMission(userId, missionId);
        if (!mission.getStatus().isActive()) {
            throw new BusinessException(ErrorCode.INVALID_MISSION_STATUS, Map.of("currentStatus", mission.getStatus().name()));
        }

        mission.abort(LocalDateTime.now());

        return new MissionAbortResponse(
                mission.getId(), mission.getStatus().name(), mission.getAbortedAt(), "산책 미션을 포기했습니다.");
    }

    @Transactional(readOnly = true)
    public CurrentMissionResponse getCurrent(Long userId) {
        return missionRecordRepository.findFirstByUserIdAndStatusInOrderByIdDesc(userId, MissionStatus.ACTIVE)
                .map(mission -> {
                    Place place = mission.getPlace();
                    return new CurrentMissionResponse(true, new CurrentMissionResponse.ActiveMissionInfo(
                            mission.getId(),
                            mission.getStatus().name(),
                            mission.getStartedAt(),
                            new CurrentMissionResponse.PlaceInfo(
                                    place.getId(), place.getKakaoPlaceId(), place.getName(), place.getCategory(),
                                    place.getRoadAddress(), place.getLatitude(), place.getLongitude()),
                            RewardPolicy.MISSION_COMPLETE_POINT));
                })
                .orElse(new CurrentMissionResponse(false, null));
    }

    private Place findNearestPlace(BigDecimal latitude, BigDecimal longitude) {
        // 위경도 사각형으로 후보를 좁힌 뒤 실제 거리로 가장 가까운 장소를 고른다.
        double latDelta = SEARCH_RADIUS_METERS / METERS_PER_DEGREE_LATITUDE;
        double lonDelta = SEARCH_RADIUS_METERS
                / (METERS_PER_DEGREE_LATITUDE * Math.cos(Math.toRadians(latitude.doubleValue())));

        return placeRepository.findByIsClosedFalseAndLatitudeBetweenAndLongitudeBetween(
                        offset(latitude, -latDelta), offset(latitude, latDelta),
                        offset(longitude, -lonDelta), offset(longitude, lonDelta))
                .stream()
                .min(Comparator.comparingDouble(place -> GeoUtils.distanceMeters(
                        latitude, longitude, place.getLatitude(), place.getLongitude())))
                .orElseThrow(() -> new BusinessException(ErrorCode.NO_NEARBY_PLACE));
    }

    private BigDecimal offset(BigDecimal value, double delta) {
        return value.add(BigDecimal.valueOf(delta)).setScale(7, RoundingMode.HALF_UP);
    }

    private MissionRecord getMission(Long userId, Long missionId) {
        return missionRecordRepository.findByIdAndUserId(missionId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MISSION_NOT_FOUND));
    }

    private void requireStatus(MissionRecord mission, MissionStatus expected) {
        if (mission.getStatus() != expected) {
            throw new BusinessException(ErrorCode.INVALID_MISSION_STATUS, Map.of("currentStatus", mission.getStatus().name()));
        }
    }
}
