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
}
