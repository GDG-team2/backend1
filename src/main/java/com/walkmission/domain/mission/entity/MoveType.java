package com.walkmission.domain.mission.entity;

/** 이동수단. speedWeight는 도보 속도 대비 배율이며 추천 거리 계산에 쓰인다. */
public enum MoveType {
    WALK("도보", 1.0),
    PUBLIC_TRANSIT("대중교통", 2.5), // 정류장까지 도보 + 대기 시간을 포함한 문 앞에서 문 앞까지 기준 (약 10km/h)
    BIKE("자전거", 3.5);

    private final String label;
    private final double speedWeight;

    MoveType(String label, double speedWeight) {
        this.label = label;
        this.speedWeight = speedWeight;
    }

    public String getLabel() { return label; }
    public double getSpeedWeight() { return speedWeight; }
}
