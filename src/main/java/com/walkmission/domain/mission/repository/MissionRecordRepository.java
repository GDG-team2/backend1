package com.walkmission.domain.mission.repository;

import com.walkmission.domain.mission.entity.AbortReason;
import com.walkmission.domain.mission.entity.MissionRecord;
import com.walkmission.domain.mission.entity.MissionStatus;
import com.walkmission.domain.mission.entity.PlaceCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MissionRecordRepository extends JpaRepository<MissionRecord, Long> {
    Optional<MissionRecord> findByIdAndUserId(Long id, Long userId);
    Optional<MissionRecord> findFirstByUserIdAndStatusInOrderByIdDesc(Long userId, Collection<MissionStatus> statuses);
    List<MissionRecord> findByUserIdAndStatus(Long userId, MissionStatus status);
    boolean existsByUserIdAndStatusIn(Long userId, Collection<MissionStatus> statuses);
    boolean existsByUserIdAndPlaceIdAndStatus(Long userId, Long placeId, MissionStatus status);
    long countByUserIdAndStatus(Long userId, MissionStatus status);
    long countByUserIdAndStatusAndCompletedAtGreaterThanEqual(Long userId, MissionStatus status, LocalDateTime since);
    boolean existsByUserIdAndStatusAndPlaceCategoryAndCompletedAtGreaterThanEqualAndIdNot(
            Long userId, MissionStatus status, PlaceCategory placeCategory, LocalDateTime since, Long excludeId);

    @Query("select count(distinct m.place.id) from MissionRecord m where m.user.id = :userId and m.status = :status")
    long countDistinctPlaces(@Param("userId") Long userId, @Param("status") MissionStatus status);

    /** 최근 완료 미션 (추천 이유: 같은 범주 연속 방문 판단) */
    List<MissionRecord> findTop3ByUserIdAndStatusOrderByCompletedAtDesc(Long userId, MissionStatus status);

    /** 같은 날 "멀었어요/피곤해요"로 포기한 적이 있는지 (다음 추천을 가깝게) */
    boolean existsByUserIdAndStatusAndAbortReasonInAndAbortedAtGreaterThanEqual(
            Long userId, MissionStatus status, Collection<AbortReason> reasons, LocalDateTime since);

    @Query("select avg(m.afterSurvey) from MissionRecord m where m.user.id = :userId and m.status = com.walkmission.domain.mission.entity.MissionStatus.COMPLETED and m.afterSurvey is not null")
    Double averageAfterSurvey(@Param("userId") Long userId);

    @Query("select avg(m.afterSurvey) from MissionRecord m where m.user.id = :userId and m.status = com.walkmission.domain.mission.entity.MissionStatus.COMPLETED and m.afterSurvey is not null and m.placeCategory = :category")
    Double averageAfterSurvey(@Param("userId") Long userId, @Param("category") PlaceCategory category);

    @Query("select count(m) from MissionRecord m where m.user.id = :userId and m.status = com.walkmission.domain.mission.entity.MissionStatus.COMPLETED and m.afterSurvey is not null and m.placeCategory = :category")
    long countSurveyed(@Param("userId") Long userId, @Param("category") PlaceCategory category);

    @Query("select distinct m.place.kakaoPlaceId from MissionRecord m where m.user.id = :userId and m.status = com.walkmission.domain.mission.entity.MissionStatus.COMPLETED")
    List<String> findAllCompletedPlaceKakaoIds(@Param("userId") Long userId);

    @Query("select distinct m.place.kakaoPlaceId from MissionRecord m where m.user.id = :userId and m.status = :status and m.completedAt >= :since")
    List<String> findPlaceKakaoIdsCompletedSince(@Param("userId") Long userId, @Param("status") MissionStatus status,
                                                 @Param("since") LocalDateTime since);
}
