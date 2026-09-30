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

    /** 추천에 사용된 범주 (랭킹 다양성 보너스 판단용) */
    @Enumerated(EnumType.STRING)
    private PlaceCategory placeCategory;

    private Boolean isNewPlace;
    private Integer stepCount;
    private Integer beforeSurvey;
    private Integer afterSurvey;

    private LocalDateTime startedAt;
    private LocalDateTime arrivedAt;
    private LocalDateTime completedAt;
    private LocalDateTime abortedAt;
    /** 도착 반경에 처음 들어온 시각. 체류 시간을 채우면 도착으로 인정한다. */
    private LocalDateTime arrivalCheckStartedAt;

    protected MissionRecord() {}

    public MissionRecord(User user, Place place, PlaceCategory placeCategory, boolean isNewPlace) {
        this.user = user;
        this.place = place;
        this.placeNameSnapshot = place.getName();
        this.placeCategory = placeCategory;
        this.status = MissionStatus.READY;
        this.isNewPlace = isNewPlace;
    }

    /** 도착 반경 안에 있음을 기록하고, 체류가 시작된 시각을 반환한다. */
    public LocalDateTime markInsideArrivalZone(LocalDateTime now) {
        requireStatus(MissionStatus.IN_PROGRESS);
        if (arrivalCheckStartedAt == null) this.arrivalCheckStartedAt = now;
        return arrivalCheckStartedAt;
    }

    /** 반경을 벗어나면 체류 시간을 처음부터 다시 잰다. */
    public void resetArrivalCheck() {
        this.arrivalCheckStartedAt = null;
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
    public PlaceCategory getPlaceCategory() { return placeCategory; }
    public Boolean getIsNewPlace() { return isNewPlace; }
    public Integer getStepCount() { return stepCount; }
    public Integer getBeforeSurvey() { return beforeSurvey; }
    public Integer getAfterSurvey() { return afterSurvey; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public LocalDateTime getArrivedAt() { return arrivedAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public LocalDateTime getAbortedAt() { return abortedAt; }
}
