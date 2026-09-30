package com.walkmission.domain.ranking.entity;

import com.walkmission.domain.user.entity.User;
import com.walkmission.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Ranking extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    private Integer userScore;

    @Column(name = "rank_position")
    private Integer rank;

    /** userScore가 속한 주의 월요일. 현재 주와 다르면 지난주 점수다. */
    private LocalDate weekStartDate;

    /** 이번 주 점수에 반영된 미션 수 (주 최대 N회) */
    private Integer scoredMissionCount;
    /** 이번 주 다양성 보너스 합계 */
    private Integer bonusScore;

    protected Ranking() {}

    public Ranking(User user, LocalDate weekStartDate) {
        this.user = user;
        this.userScore = 0;
        this.scoredMissionCount = 0;
        this.bonusScore = 0;
        this.weekStartDate = weekStartDate;
    }

    public void addMissionScore(int missionScore, int bonus) {
        this.userScore += missionScore + bonus;
        this.scoredMissionCount = getScoredMissionCount() + 1;
        this.bonusScore = getBonusScore() + bonus;
    }

    public void resetForWeek(LocalDate weekStartDate) {
        this.userScore = 0;
        this.scoredMissionCount = 0;
        this.bonusScore = 0;
        this.rank = null;
        this.weekStartDate = weekStartDate;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public Integer getUserScore() { return userScore; }
    public Integer getRank() { return rank; }
    public LocalDate getWeekStartDate() { return weekStartDate; }
    public int getScoredMissionCount() { return scoredMissionCount != null ? scoredMissionCount : 0; }
    public int getBonusScore() { return bonusScore != null ? bonusScore : 0; }
}
