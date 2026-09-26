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
}
