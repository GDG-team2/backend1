package com.walkmission.domain.user.entity;

import com.walkmission.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class UserProfile extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    /** 주간 목표를 연속으로 달성한 주 수 */
    private Integer rhythmWeeks;
    private Integer bestRhythmWeeks;
    /** 마지막으로 주간 목표를 달성한 주의 월요일 */
    private LocalDate lastRhythmWeekStart;

    private Integer currentPoint;
    private Long representativeBadgeId;

    protected UserProfile() {}

    public UserProfile(User user) {
        this.user = user;
        this.rhythmWeeks = 0;
        this.bestRhythmWeeks = 0;
        this.currentPoint = 0;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public Integer getCurrentPoint() { return currentPoint; }
    public Long getRepresentativeBadgeId() { return representativeBadgeId; }
    public int getBestRhythmWeeks() { return bestRhythmWeeks != null ? bestRhythmWeeks : 0; }
    public LocalDate getLastRhythmWeekStart() { return lastRhythmWeekStart; }

    /**
     * 이번 주 목표 달성을 기록한다. 이미 이번 주에 달성했으면 아무것도 하지 않고 false를 반환한다.
     * 지난주에도 달성했으면 연속 주 수를 이어가고, 아니면 1주부터 다시 시작한다.
     */
    public boolean achieveWeeklyGoal(LocalDate weekStart) {
        if (weekStart.equals(lastRhythmWeekStart)) return false;
        boolean continued = weekStart.minusWeeks(1).equals(lastRhythmWeekStart);
        this.rhythmWeeks = continued ? getRhythmWeeksRaw() + 1 : 1;
        this.lastRhythmWeekStart = weekStart;
        this.bestRhythmWeeks = Math.max(getBestRhythmWeeks(), rhythmWeeks);
        return true;
    }

    /**
     * 현재 이어지고 있는 리듬(주). 이번 주나 지난주에 달성했으면 유지 중으로 보고,
     * 지난주를 놓쳤으면 이미 끊긴 것으로 본다. 이번 주는 아직 진행 중이라 끊긴 것으로 보지 않는다.
     */
    public int getRhythmWeeksAsOf(LocalDate currentWeekStart) {
        if (lastRhythmWeekStart == null || lastRhythmWeekStart.isBefore(currentWeekStart.minusWeeks(1))) return 0;
        return getRhythmWeeksRaw();
    }

    private int getRhythmWeeksRaw() {
        return rhythmWeeks != null ? rhythmWeeks : 0;
    }

    public void addPoint(int amount) {
        this.currentPoint += amount;
    }

    public void changeRepresentativeBadge(Long badgeId) {
        this.representativeBadgeId = badgeId;
    }
}
