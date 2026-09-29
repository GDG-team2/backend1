package com.walkmission.domain.user.entity;

import jakarta.persistence.*;

@Entity
public class UserMissionPreference {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    private Integer walkTime;
    private Integer spendLimit;
    private String howMove;
    private String preference;
    
    protected UserMissionPreference() {}

    public UserMissionPreference(User user) {
        this.user = user;
        this.walkTime = 30;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public Integer getWalkTime() { return walkTime; }
    public Integer getSpendLimit() { return spendLimit; }
    public String getHowMove() { return howMove; }
    public String getPreference() { return preference; }
}
