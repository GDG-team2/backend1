package com.walkmission.domain.mission.entity;

/**
 * 사용자가 고르는 미션 장소 범주.
 * stayMinutes: 장소에서 보내는 기본 시간 (전체 외출 시간에서 이동에 쓸 시간을 계산할 때 뺀다)
 * estimatedCost: 예상 비용(원). 카카오 장소 검색은 가격을 주지 않아 범주 평균으로 잡는다.
 */
public enum PlaceCategory {
    WALK("산책", 15, 0),
    CAFE("카페", 25, 6_000),
    SIGHTSEEING("구경", 20, 0),
    EXHIBITION("전시", 30, 10_000),
    FOOD("먹기", 35, 12_000);

    private final String label;
    private final int stayMinutes;
    private final int estimatedCost;

    PlaceCategory(String label, int stayMinutes, int estimatedCost) {
        this.label = label;
        this.stayMinutes = stayMinutes;
        this.estimatedCost = estimatedCost;
    }

    public String getLabel() { return label; }
    public int getStayMinutes() { return stayMinutes; }
    public int getEstimatedCost() { return estimatedCost; }
}
