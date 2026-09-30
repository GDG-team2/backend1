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

    private Integer streakNow;
    private Integer streakRecord;
    private LocalDate lastActiveDate;
    private Integer currentPoint;
    private Long representativeBadgeId;

    protected UserProfile() {}

    public UserProfile(User user) {
        this.user = user;
        this.streakNow = 0;
        this.streakRecord = 0;
        this.currentPoint = 0;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public Integer getStreakNow() { return streakNow; }
    public Integer getStreakRecord() { return streakRecord; }
    public LocalDate getLastActiveDate() { return lastActiveDate; }
    public Integer getCurrentPoint() { return currentPoint; }
    public Long getRepresentativeBadgeId() { return representativeBadgeId; }

    /**
     * 오늘 미션을 완료했음을 기록하고 스트릭이 이어졌는지 반환한다.
     * 어제(또는 오늘 이미) 활동했으면 유지, 그 외에는 1일부터 다시 시작한다.
     */
    public boolean recordActivity(LocalDate today) {
        boolean maintained = lastActiveDate != null && !lastActiveDate.isBefore(today.minusDays(1));
        if (lastActiveDate == null || !lastActiveDate.equals(today)) {
            this.streakNow = maintained ? streakNow + 1 : 1;
            this.lastActiveDate = today;
        }
        this.streakRecord = Math.max(streakRecord, streakNow);
        return maintained;
    }

    /** 어제 이후로 활동이 없으면 스트릭은 이미 끊긴 것으로 본다. */
    public int getStreakAsOf(LocalDate today) {
        if (lastActiveDate == null || lastActiveDate.isBefore(today.minusDays(1))) return 0;
        return streakNow;
    }

    public void addPoint(int amount) {
        this.currentPoint += amount;
    }

    public void changeRepresentativeBadge(Long badgeId) {
        this.representativeBadgeId = badgeId;
    }
}
