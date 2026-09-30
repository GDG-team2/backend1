package com.walkmission.domain.ranking.entity;

import com.walkmission.domain.user.entity.User;
import com.walkmission.global.entity.BaseTimeEntity;
import jakarta.persistence.*;

@Entity
public class Ranking extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    private Integer userScore;
    
    @Column(name = "rank_position")
    private Integer rank;

    protected Ranking() {}
}
