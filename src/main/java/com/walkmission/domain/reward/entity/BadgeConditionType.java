package com.walkmission.domain.reward.entity;

public enum BadgeConditionType {
    /** 누적 미션 완료 횟수 */
    MISSION_COUNT,
    /** 완료한 서로 다른 장소 수 */
    PLACE_COUNT,
    /** 주간 목표를 연속으로 달성한 최장 주 수 */
    RHYTHM_WEEKS
}
