package com.walkmission.domain.mission.repository;

import com.walkmission.domain.mission.entity.AbortReason;
import com.walkmission.domain.mission.entity.MissionRecord;
import com.walkmission.domain.mission.entity.MissionStatus;
import com.walkmission.domain.mission.entity.PlaceCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MissionRecordRepository extends JpaRepository<MissionRecord, Long> {
    Optional<MissionRecord> findByIdAndUserId(Long id, Long userId);

    /** 상태를 바꾸는 요청용: 같은 미션에 동시에 온 요청은 앞 요청이 끝날 때까지 기다렸다가 바뀐 상태를 본다 (중복 정산 방지) */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from MissionRecord m where m.id = :id and m.user.id = :userId")
    Optional<MissionRecord> findByIdAndUserIdForUpdate(@Param("id") Long id, @Param("userId") Long userId);
    Optional<MissionRecord> findFirstByUserIdAndStatusInOrderByIdDesc(Long userId, Collection<MissionStatus> statuses);
    List<MissionRecord> findByUserIdAndStatus(Long userId, MissionStatus status);
    /** 출발을 약속하지 않은 추천 (다시 추천받으면 대체되는 대상) */
    List<MissionRecord> findByUserIdAndStatusAndScheduledAtIsNull(Long userId, MissionStatus status);
    Optional<MissionRecord> findFirstByUserIdAndStatusAndScheduledAtIsNullOrderByIdDesc(Long userId, MissionStatus status);
    /** 출발을 약속한 미션 (가까운 시각 순) */
    List<MissionRecord> findByUserIdAndStatusAndScheduledAtIsNotNullOrderByScheduledAtAsc(Long userId, MissionStatus status);
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

    // ---- 기록 조회 (기간·범주 필터는 항상 값을 넘긴다: 전체면 모든 범주) ----
    String COMPLETED_IN_RANGE = " from MissionRecord m where m.user.id = :userId"
            + " and m.status = com.walkmission.domain.mission.entity.MissionStatus.COMPLETED"
            + " and m.completedAt >= :from and m.completedAt < :to and m.placeCategory in :categories";

    @Query(value = "select m" + COMPLETED_IN_RANGE + " order by m.completedAt desc",
            countQuery = "select count(m)" + COMPLETED_IN_RANGE)
    Page<MissionRecord> findCompletedHistory(@Param("userId") Long userId, @Param("from") LocalDateTime from,
                                             @Param("to") LocalDateTime to,
                                             @Param("categories") Collection<PlaceCategory> categories,
                                             Pageable pageable);

    @Query("select new com.walkmission.domain.mission.repository.HistoryStats(count(m), coalesce(sum(m.durationMinutes), 0),"
            + " avg(m.afterSurvey), coalesce(sum(m.distanceMeters), 0),"
            + " coalesce(sum(case when m.isNewPlace = true then 1 else 0 end), 0))" + COMPLETED_IN_RANGE)
    HistoryStats aggregateCompleted(@Param("userId") Long userId, @Param("from") LocalDateTime from,
                                    @Param("to") LocalDateTime to,
                                    @Param("categories") Collection<PlaceCategory> categories);

    @Query("select new com.walkmission.domain.mission.repository.VisitedPlaceRow(p.id, p.name, p.category, p.latitude, p.longitude,"
            + " count(m), max(m.completedAt)) from MissionRecord m join m.place p where m.user.id = :userId"
            + " and m.status = com.walkmission.domain.mission.entity.MissionStatus.COMPLETED"
            + " and m.completedAt >= :from and m.completedAt < :to"
            + " group by p.id, p.name, p.category, p.latitude, p.longitude order by max(m.completedAt) desc")
    List<VisitedPlaceRow> findVisitedPlaces(@Param("userId") Long userId, @Param("from") LocalDateTime from,
                                            @Param("to") LocalDateTime to);

    @Query("select distinct m.place.kakaoPlaceId from MissionRecord m where m.user.id = :userId and m.status = :status and m.completedAt >= :since")
    List<String> findPlaceKakaoIdsCompletedSince(@Param("userId") Long userId, @Param("status") MissionStatus status,
                                                 @Param("since") LocalDateTime since);
}
