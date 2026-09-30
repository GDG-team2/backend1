package com.walkmission.domain.reward.entity;

import com.walkmission.global.entity.BaseTimeEntity;
import jakarta.persistence.*;

@Entity
public class Badge extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String badgeName;

    private String description;

    private String iconUrl;

    @Column(columnDefinition = "TEXT")
    private String badgeCondition;

    protected Badge() {}

    public Badge(String badgeName, String description, String iconUrl, String badgeCondition) {
        this.badgeName = badgeName;
        this.description = description;
        this.iconUrl = iconUrl;
        this.badgeCondition = badgeCondition;
    }

    public Long getId() { return id; }
    public String getBadgeName() { return badgeName; }
    public String getDescription() { return description; }
    public String getIconUrl() { return iconUrl; }
    public String getBadgeCondition() { return badgeCondition; }
}
