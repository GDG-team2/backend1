package com.walkmission.domain.mission.entity;

/** 산책을 중간에 멈출 때 고르는 이유. 같은 날 다음 추천에 반영한다. */
public enum AbortReason {
    FARTHER_THAN_EXPECTED("생각보다 멀었어요", true),
    TIRED("피곤해졌어요", true),
    SOMETHING_CAME_UP("갑자기 일이 생겼어요", false),
    ROUTE_INCONVENIENT("길이 불편했어요", false);

    private final String label;
    /** 다음 추천을 더 가깝게 할지 */
    private final boolean shortensNextMission;

    AbortReason(String label, boolean shortensNextMission) {
        this.label = label;
        this.shortensNextMission = shortensNextMission;
    }

    public String getLabel() { return label; }
    public boolean shortensNextMission() { return shortensNextMission; }
}
