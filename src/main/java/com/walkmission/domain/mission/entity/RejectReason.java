package com.walkmission.domain.mission.entity;

/** 추천을 다시 받을 때 고르는 거절 이유. 다음 추천에 바로 반영한다. */
public enum RejectReason {
    TOO_FAR("너무 멀어요"),
    DISLIKE_ACTIVITY("이 활동은 싫어요"),
    ALREADY_VISITED("이미 가본 곳이에요"),
    NO_SPENDING("지금은 돈 쓰기 싫어요");

    private final String label;

    RejectReason(String label) {
        this.label = label;
    }

    public String getLabel() { return label; }
}
