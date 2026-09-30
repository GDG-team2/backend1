package com.walkmission.domain.mission.entity;

/** 사용자가 고르는 미션 장소 범주 */
public enum PlaceCategory {
    WALK("산책"),
    CAFE("카페"),
    SIGHTSEEING("구경"),
    EXHIBITION("전시"),
    FOOD("먹기");

    private final String label;

    PlaceCategory(String label) {
        this.label = label;
    }

    public String getLabel() { return label; }
}
