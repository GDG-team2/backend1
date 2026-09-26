package com.walkmission.domain.mission.entity;

import com.walkmission.domain.user.entity.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class MissionRecord {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    private String placeNameSnapshot;

    @Enumerated(EnumType.STRING)
    private MissionStatus status;

    private Boolean isNewPlace;
    private Integer stepCount;
    private Integer beforeSurvey;
    private Integer afterSurvey;

    private LocalDateTime startedAt;
    private LocalDateTime arrivedAt;
    private LocalDateTime completedAt;

    protected MissionRecord() {}
}
