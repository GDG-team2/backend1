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

    /** 화면에 보이는 미션 제목 (예: "화랑유원지 한 바퀴") */
    private String missionTitle;

    /** 추천 이유 (예: "최근 카페 방문이 3회 연속이었어요.") */
    private String recommendReason;

    @Enumerated(EnumType.STRING)
    private MissionStatus status;

    /** 추천에 사용된 범주 (랭킹 다양성 보너스 판단용) */
    @Enumerated(EnumType.STRING)
    private PlaceCategory placeCategory;

    /** 추천받을 때 고른 기분. beforeSurvey는 이를 1~5로 환산한 값 */
    @Enumerated(EnumType.STRING)
    private Mood beforeMood;

    private Boolean isNewPlace;
    private Integer stepCount;
    private Integer beforeSurvey;
    private Integer afterSurvey;

    /** 다시 추천받으며 거절한 이유 (다시 추천으로 대체된 미션만) */
    @Enumerated(EnumType.STRING)
    private RejectReason rejectReason;

    /** 산책 중 포기한 이유와 그때까지 이동한 거리 */
    @Enumerated(EnumType.STRING)
    private AbortReason abortReason;
    private Integer movedDistanceMeters;

    private LocalDateTime startedAt;
    private LocalDateTime arrivedAt;
    private LocalDateTime completedAt;
    private LocalDateTime abortedAt;
    /** 도착 반경에 처음 들어온 시각. 체류 시간을 채우면 도착으로 인정한다. */
    private LocalDateTime arrivalCheckStartedAt;

    protected MissionRecord() {}

    public MissionRecord(User user, Place place, PlaceCategory placeCategory, boolean isNewPlace,
                         String missionTitle, String recommendReason, Mood beforeMood) {
        this.user = user;
        this.place = place;
        this.placeNameSnapshot = place.getName();
        this.placeCategory = placeCategory;
        this.isNewPlace = isNewPlace;
        this.missionTitle = missionTitle;
        this.recommendReason = recommendReason;
        this.beforeMood = beforeMood;
        this.beforeSurvey = beforeMood != null ? beforeMood.getScore() : null;
        this.status = MissionStatus.READY;
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

    public void start(LocalDateTime now) {
        requireStatus(MissionStatus.READY);
        this.status = MissionStatus.IN_PROGRESS;
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

    /** 다시 추천을 받아 출발 전 미션을 대체한다. 이유가 있으면 남긴다. */
    public void replaceByNewRecommendation(RejectReason reason, LocalDateTime now) {
        requireStatus(MissionStatus.READY);
        this.rejectReason = reason;
        this.status = MissionStatus.ABORTED;
        this.abortedAt = now;
    }

    public void abort(AbortReason reason, Integer movedDistanceMeters, LocalDateTime now) {
        if (!status.isActive()) {
            throw new IllegalStateException("Mission is not active: " + status);
        }
        this.abortReason = reason;
        this.movedDistanceMeters = movedDistanceMeters;
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
    public String getMissionTitle() { return missionTitle != null ? missionTitle : placeNameSnapshot; }
    public String getRecommendReason() { return recommendReason; }
    public MissionStatus getStatus() { return status; }
    public PlaceCategory getPlaceCategory() { return placeCategory; }
    public Mood getBeforeMood() { return beforeMood; }
    public Boolean getIsNewPlace() { return isNewPlace; }
    public Integer getStepCount() { return stepCount; }
    public Integer getBeforeSurvey() { return beforeSurvey; }
    public Integer getAfterSurvey() { return afterSurvey; }
    public RejectReason getRejectReason() { return rejectReason; }
    public AbortReason getAbortReason() { return abortReason; }
    public Integer getMovedDistanceMeters() { return movedDistanceMeters; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public LocalDateTime getArrivedAt() { return arrivedAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public LocalDateTime getAbortedAt() { return abortedAt; }
}
