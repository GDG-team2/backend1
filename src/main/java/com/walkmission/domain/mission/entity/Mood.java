package com.walkmission.domain.mission.entity;

import java.util.EnumSet;
import java.util.Set;

/**
 * 추천 전에 고르는 지금 기분. 추천 거리와 범주 우선순위에 반영한다.
 * score는 기록 상세의 "기분 변화(전 → 후)"를 위한 1~5 환산값.
 */
public enum Mood {
    TIRED("지침", 0.7, EnumSet.of(PlaceCategory.CAFE, PlaceCategory.WALK), false, 2),
    BORED("심심함", 1.0, EnumSet.noneOf(PlaceCategory.class), true, 3),
    GLOOMY("꿀꿀함", 1.0, EnumSet.noneOf(PlaceCategory.class), true, 2),
    ENERGETIC("활기참", 1.2, EnumSet.of(PlaceCategory.SIGHTSEEING, PlaceCategory.EXHIBITION), false, 4);

    private final String label;
    private final double distanceFactor;
    private final Set<PlaceCategory> preferredCategories;
    private final boolean prefersNewPlace;
    private final int score;

    Mood(String label, double distanceFactor, Set<PlaceCategory> preferredCategories, boolean prefersNewPlace, int score) {
        this.label = label;
        this.distanceFactor = distanceFactor;
        this.preferredCategories = preferredCategories;
        this.prefersNewPlace = prefersNewPlace;
        this.score = score;
    }

    public String getLabel() { return label; }
    public double getDistanceFactor() { return distanceFactor; }
    public Set<PlaceCategory> getPreferredCategories() { return preferredCategories; }
    public boolean prefersNewPlace() { return prefersNewPlace; }
    public int getScore() { return score; }
}
