package com.walkmission.domain.reward.entity;

import jakarta.persistence.*;

@Entity
public class Badge {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String badgeName;
    
    @Column(columnDefinition = "TEXT")
    private String badgeCondition;

    protected Badge() {}
}
