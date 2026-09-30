package com.walkmission.domain.reward;

/** 보상 수치 모음. 가챠/제휴 상품 가격이 정해지면 여기서 조정한다. */
public final class RewardPolicy {
    public static final int MISSION_COMPLETE_POINT = 50;

    /** 랭킹: 미션 1회당 점수. 거리·속도는 점수에 반영하지 않는다. */
    public static final int MISSION_COMPLETE_RANKING_SCORE = 100;
    /** 랭킹: 과도한 경쟁을 막기 위해 주 N회까지만 점수에 반영 */
    public static final int MAX_SCORED_MISSIONS_PER_WEEK = 3;
    /** 랭킹: 그 주에 처음 해본 범주면 추가 점수 (활동 다양성 보상) */
    public static final int NEW_CATEGORY_BONUS_SCORE = 20;

    private RewardPolicy() {}
}
