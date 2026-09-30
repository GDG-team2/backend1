package com.walkmission.domain.reward.repository;

import com.walkmission.domain.reward.entity.PointHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {
}
