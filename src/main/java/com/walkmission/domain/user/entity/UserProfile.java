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

    public void changeRepresentativeBadge(Long badgeId) {
        this.representativeBadgeId = badgeId;
    }
}
