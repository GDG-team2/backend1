package com.walkmission.domain.mission.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
public class Place {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String kakaoPlaceId;

    private String name;
    private String roadAddress;
    private String category;
    
    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;
    
    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    private Boolean isSponsored;
    private String rewardInfo;
    private Boolean isClosed;

    protected Place() {}
}
