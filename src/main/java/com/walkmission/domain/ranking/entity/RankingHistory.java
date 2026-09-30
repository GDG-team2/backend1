package com.walkmission.domain.ranking.entity;

import com.walkmission.domain.user.entity.User;
import com.walkmission.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class RankingHistory extends BaseTimeEntity {

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

    public RankingHistory(User user, Integer userRank, Integer userScore, LocalDate startDate, LocalDate endDate) {
        this.user = user;
        this.userRank = userRank;
        this.userScore = userScore;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public Integer getUserRank() { return userRank; }
    public Integer getUserScore() { return userScore; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
}
