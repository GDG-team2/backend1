package com.walkmission.domain.mission.entity;

import java.util.List;

public enum MissionStatus {
    READY, IN_PROGRESS, ARRIVED, COMPLETED, ABORTED;

    /** 아직 끝나지 않은(복구 대상) 미션 상태 */
    public static final List<MissionStatus> ACTIVE = List.of(READY, IN_PROGRESS, ARRIVED);

    /** 산책 중인 상태. 이 상태의 미션은 사용자당 하나만 허용 */
    public static final List<MissionStatus> WALKING = List.of(IN_PROGRESS, ARRIVED);

    public boolean isActive() {
        return ACTIVE.contains(this);
    }
}
