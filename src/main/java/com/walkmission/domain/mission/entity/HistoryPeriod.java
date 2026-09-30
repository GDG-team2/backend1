package com.walkmission.domain.mission.entity;

/** 기록 조회 기간 */
public enum HistoryPeriod {
    WEEK("이번 주"),
    MONTH("월간"),
    ALL("전체");

    private final String label;

    HistoryPeriod(String label) {
        this.label = label;
    }

    public String getLabel() { return label; }
}
