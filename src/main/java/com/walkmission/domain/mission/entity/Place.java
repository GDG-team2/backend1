package com.walkmission.domain.mission.entity;

import com.walkmission.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
public class Place extends BaseTimeEntity {

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
    private String placeUrl;

    protected Place() {}

    public Place(String kakaoPlaceId, String name, String roadAddress, String category,
                 BigDecimal latitude, BigDecimal longitude, String placeUrl) {
        this.kakaoPlaceId = kakaoPlaceId;
        this.name = name;
        this.roadAddress = roadAddress;
        this.category = category;
        this.latitude = latitude;
        this.longitude = longitude;
        this.isSponsored = false;
        this.isClosed = false;
        this.placeUrl = placeUrl;
    }

    public Long getId() { return id; }
    public String getKakaoPlaceId() { return kakaoPlaceId; }
    public String getName() { return name; }
    public String getRoadAddress() { return roadAddress; }
    public String getCategory() { return category; }
    public BigDecimal getLatitude() { return latitude; }
    public BigDecimal getLongitude() { return longitude; }
    public Boolean getIsSponsored() { return isSponsored; }
    public String getRewardInfo() { return rewardInfo; }
    public Boolean getIsClosed() { return isClosed; }
    public String getPlaceUrl() { return placeUrl; }
}
