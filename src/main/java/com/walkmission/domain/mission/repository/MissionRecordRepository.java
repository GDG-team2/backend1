package com.walkmission.domain.mission.repository;

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

    @Query("select distinct m.place.kakaoPlaceId from MissionRecord m where m.user.id = :userId and m.status = :status and m.completedAt >= :since")
    List<String> findPlaceKakaoIdsCompletedSince(@Param("userId") Long userId, @Param("status") MissionStatus status,
                                                 @Param("since") LocalDateTime since);
}
