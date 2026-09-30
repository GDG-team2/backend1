package com.walkmission.domain.reward.entity;

import com.walkmission.domain.user.entity.User;
import com.walkmission.global.entity.BaseTimeEntity;
import jakarta.persistence.*;

@Entity
public class PointHistory extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private Integer amount;

    @Enumerated(EnumType.STRING)
    private PointType type;

    private String description;

    protected PointHistory() {}

    public PointHistory(User user, Integer amount, PointType type, String description) {
        this.user = user;
        this.amount = amount;
        this.type = type;
        this.description = description;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public Integer getAmount() { return amount; }
    public PointType getType() { return type; }
    public String getDescription() { return description; }
}
