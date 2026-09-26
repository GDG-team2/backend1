package com.walkmission.domain.ranking.entity;

import com.walkmission.domain.user.entity.User;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class RankingHistory {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private Integer userRank;
    private Integer userScore;
    
    private LocalDate startDate;
    private LocalDate endDate;

    protected RankingHistory() {}
}
