package com.walkmission.domain.ranking.repository;

import com.walkmission.domain.ranking.entity.Ranking;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RankingRepository extends JpaRepository<Ranking, Long> {
    Optional<Ranking> findByUserId(Long userId);

    boolean existsByWeekStartDateBefore(LocalDate weekStartDate);

    @Query("select r from Ranking r join fetch r.user where r.weekStartDate < :weekStart")
    List<Ranking> findAllBeforeWeek(@Param("weekStart") LocalDate weekStart);

    /** 랭킹 공개를 켠 같은 동네 사용자의 이번 주 점수 (높은 순, 동점이면 먼저 달성한 순) */
    @Query("""
            select r from Ranking r join fetch r.user u
            where u.regionCode = :regionCode and r.weekStartDate = :weekStart and r.userScore > 0
              and exists (select s.id from UserSetting s where s.user = u and s.rankingSetting = true)
            order by r.userScore desc, r.updatedAt asc
            """)
    List<Ranking> findLeaderboard(@Param("regionCode") String regionCode,
                                  @Param("weekStart") LocalDate weekStart, Pageable pageable);

    @Query("""
            select count(r) from Ranking r join r.user u
            where u.regionCode = :regionCode and r.weekStartDate = :weekStart and r.userScore > :score
              and exists (select s.id from UserSetting s where s.user = u and s.rankingSetting = true)
            """)
    long countHigherScores(@Param("regionCode") String regionCode,
                           @Param("weekStart") LocalDate weekStart, @Param("score") int score);
}
