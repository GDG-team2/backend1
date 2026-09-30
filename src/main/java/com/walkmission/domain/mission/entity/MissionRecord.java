package com.walkmission.domain.mission.entity;

import com.walkmission.domain.user.entity.User;
import com.walkmission.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class MissionRecord extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    private String placeNameSnapshot;

    @Enumerated(EnumType.STRING)
    private MissionStatus status;

    private Boolean isNewPlace;
    private Integer stepCount;
    private Integer beforeSurvey;
    private Integer afterSurvey;

    private LocalDateTime startedAt;
    private LocalDateTime arrivedAt;
    private LocalDateTime completedAt;
    private LocalDateTime abortedAt;

    protected MissionRecord() {}

    public MissionRecord(User user, Place place, boolean isNewPlace) {
        this.user = user;
        this.place = place;
        this.placeNameSnapshot = place.getName();
        this.status = MissionStatus.READY;
        this.isNewPlace = isNewPlace;
    }

    public void start(Integer beforeSurvey, LocalDateTime now) {
        requireStatus(MissionStatus.READY);
        this.status = MissionStatus.IN_PROGRESS;
        this.beforeSurvey = beforeSurvey;
        this.startedAt = now;
    }

    public void arrive(LocalDateTime now) {
        requireStatus(MissionStatus.IN_PROGRESS);
        this.status = MissionStatus.ARRIVED;
        this.arrivedAt = now;
    }

    public void complete(Integer afterSurvey, Integer stepCount, LocalDateTime now) {
        requireStatus(MissionStatus.ARRIVED);
        this.status = MissionStatus.COMPLETED;
        this.afterSurvey = afterSurvey;
        this.stepCount = stepCount;
        this.completedAt = now;
    }

    public void abort(LocalDateTime now) {
        if (!status.isActive()) {
            throw new IllegalStateException("Mission is not active: " + status);
        }
        this.status = MissionStatus.ABORTED;
        this.abortedAt = now;
    }

    private void requireStatus(MissionStatus expected) {
        if (this.status != expected) {
            throw new IllegalStateException("Expected " + expected + " but was " + status);
        }
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public Place getPlace() { return place; }
    public String getPlaceNameSnapshot() { return placeNameSnapshot; }
    public MissionStatus getStatus() { return status; }
    public Boolean getIsNewPlace() { return isNewPlace; }
    public Integer getStepCount() { return stepCount; }
    public Integer getBeforeSurvey() { return beforeSurvey; }
    public Integer getAfterSurvey() { return afterSurvey; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public LocalDateTime getArrivedAt() { return arrivedAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public LocalDateTime getAbortedAt() { return abortedAt; }
}
