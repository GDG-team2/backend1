package com.walkmission.domain.reward.repository;

import com.walkmission.domain.reward.entity.PointHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {
    Page<PointHistory> findByUserIdOrderByIdDesc(Long userId, Pageable pageable);
}
