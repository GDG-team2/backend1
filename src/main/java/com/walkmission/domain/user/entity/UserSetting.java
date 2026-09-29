package com.walkmission.domain.user.entity;

import com.walkmission.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import java.time.LocalTime;

@Entity
public class UserSetting extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    private Boolean allAlarm;
    private Boolean startAlarm;
    private Boolean missionAlarm;
    private Boolean insightAlarm;
    private Boolean rewardAlarm;
    
    private LocalTime quietStart;
    private LocalTime quietEnd;
    
    private Boolean rankingSetting;
    private Boolean nameSetting;
    private Boolean placeSetting;
    private Boolean friendSetting;

    protected UserSetting() {}

    public UserSetting(User user) {
        this.user = user;
        this.allAlarm = true;
        this.startAlarm = true;
        this.missionAlarm = true;
        this.insightAlarm = true;
        this.rewardAlarm = true;
        this.rankingSetting = true;
        this.nameSetting = true;
        this.placeSetting = true;
        this.friendSetting = true;
    }
    
    public void update(LocalTime quietStart, LocalTime quietEnd, Boolean allAlarm) {
        if (quietStart != null) this.quietStart = quietStart;
        if (quietEnd != null) this.quietEnd = quietEnd;
        if (allAlarm != null) this.allAlarm = allAlarm;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public Boolean getAllAlarm() { return allAlarm; }
    public Boolean getStartAlarm() { return startAlarm; }
    public Boolean getMissionAlarm() { return missionAlarm; }
    public Boolean getInsightAlarm() { return insightAlarm; }
    public Boolean getRewardAlarm() { return rewardAlarm; }
    public LocalTime getQuietStart() { return quietStart; }
    public LocalTime getQuietEnd() { return quietEnd; }
    public Boolean getRankingSetting() { return rankingSetting; }
    public Boolean getNameSetting() { return nameSetting; }
    public Boolean getPlaceSetting() { return placeSetting; }
    public Boolean getFriendSetting() { return friendSetting; }
}
