package com.walkmission.domain.ranking.repository;

import com.walkmission.domain.ranking.entity.RankingHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RankingHistoryRepository extends JpaRepository<RankingHistory, Long> {
}
