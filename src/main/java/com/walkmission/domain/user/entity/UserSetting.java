package com.walkmission.domain.user.entity;

import com.walkmission.global.entity.BaseTimeEntity;
import com.walkmission.domain.user.dto.UserSettingsUpdateRequest;
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
    
    /** 조용한 시간 사용 여부. 꺼져 있으면 quietStart~quietEnd는 저장만 되고 적용되지 않는다 */
    private Boolean quietEnabled;
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
    
    public void update(UserSettingsUpdateRequest request) {
        if (request.allAlarm() != null) this.allAlarm = request.allAlarm();
        if (request.startAlarm() != null) this.startAlarm = request.startAlarm();
        if (request.missionAlarm() != null) this.missionAlarm = request.missionAlarm();
        if (request.insightAlarm() != null) this.insightAlarm = request.insightAlarm();
        if (request.rewardAlarm() != null) this.rewardAlarm = request.rewardAlarm();
        if (request.quietEnabled() != null) this.quietEnabled = request.quietEnabled();
        if (request.quietStart() != null) this.quietStart = request.quietStart();
        if (request.quietEnd() != null) this.quietEnd = request.quietEnd();
        if (request.rankingSetting() != null) this.rankingSetting = request.rankingSetting();
        if (request.nameSetting() != null) this.nameSetting = request.nameSetting();
        if (request.placeSetting() != null) this.placeSetting = request.placeSetting();
        if (request.friendSetting() != null) this.friendSetting = request.friendSetting();
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public Boolean getAllAlarm() { return allAlarm; }
    public Boolean getStartAlarm() { return startAlarm; }
    public Boolean getMissionAlarm() { return missionAlarm; }
    public Boolean getInsightAlarm() { return insightAlarm; }
    public Boolean getRewardAlarm() { return rewardAlarm; }
    public Boolean getQuietEnabled() { return Boolean.TRUE.equals(quietEnabled); }
    public LocalTime getQuietStart() { return quietStart; }
    public LocalTime getQuietEnd() { return quietEnd; }
    public Boolean getRankingSetting() { return rankingSetting; }
    public Boolean getNameSetting() { return nameSetting; }
    public Boolean getPlaceSetting() { return placeSetting; }
    public Boolean getFriendSetting() { return friendSetting; }
}
