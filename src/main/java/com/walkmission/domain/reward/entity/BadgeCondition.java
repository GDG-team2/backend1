package com.walkmission.domain.reward.entity;

/** badge.badge_condition 컬럼에 JSON으로 저장되는 획득 조건. 예: {"type":"STREAK","threshold":3} */
public record BadgeCondition(BadgeConditionType type, int threshold) {}
