package com.walkmission.domain.mission.entity;

/** 부담 없는 예산. 범주의 예상 비용이 한도를 넘으면 추천에서 뺀다. */
public enum Budget {
    FREE("0원", 0),
    UNDER_10K("1만원", 10_000),
    UNDER_30K("3만원", 30_000),
    ANY("상관없음", null);

    private final String label;
    private final Integer limit;

    Budget(String label, Integer limit) {
        this.label = label;
        this.limit = limit;
    }

    public String getLabel() { return label; }
    public Integer getLimit() { return limit; }

    public boolean allows(int cost) {
        return limit == null || cost <= limit;
    }

    /** DB의 spend_limit(원) 값을 예산 선택지로 바꾼다. null이면 상관없음 */
    public static Budget fromLimit(Integer limit) {
        if (limit == null) return ANY;
        for (Budget budget : values()) {
            if (budget.limit != null && budget.limit.equals(limit)) return budget;
        }
        return ANY;
    }
}
