package com.walkmission.domain.mission.repository;

import com.walkmission.domain.mission.entity.MissionRecord;
import com.walkmission.domain.mission.entity.MissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
