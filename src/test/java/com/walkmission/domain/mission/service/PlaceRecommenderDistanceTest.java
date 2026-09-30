package com.walkmission.domain.mission.service;

import com.walkmission.domain.mission.entity.MoveType;
import com.walkmission.domain.mission.entity.PlaceCategory;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/** 전체 외출 시간 → 추천 거리, 예상 시간 계산 규칙 */
class PlaceRecommenderDistanceTest {

    @Test
    void 이동_시간은_전체_외출_시간에서_장소에서_보내는_시간을_뺀다() {
        assertThat(PlaceRecommender.travelMinutes(60, PlaceCategory.WALK)).isEqualTo(45);      // 산책 15분
        assertThat(PlaceRecommender.travelMinutes(60, PlaceCategory.FOOD)).isEqualTo(25);      // 먹기 35분
        assertThat(PlaceRecommender.travelMinutes(30, PlaceCategory.EXHIBITION)).isZero();     // 전시 30분
    }

    @Test
    void 최대_직선거리는_편도_시간_도보속도_우회계수로_계산한다() {
        // 이동 45분 → 편도 22.5분 × 분당 67m = 1507.5m, ÷ 우회 1.3 = 약 1159.6m
        assertThat(PlaceRecommender.maxStraightDistance(45, MoveType.WALK, 1.0)).isCloseTo(1159.6, within(0.1));
    }

    @Test
    void 이동수단_가중치와_거리_보정이_곱해진다() {
        double walk = PlaceRecommender.maxStraightDistance(45, MoveType.WALK, 1.0);
        assertThat(PlaceRecommender.maxStraightDistance(45, MoveType.BIKE, 1.0)).isCloseTo(walk * 3.5, within(0.01));
        assertThat(PlaceRecommender.maxStraightDistance(45, MoveType.PUBLIC_TRANSIT, 1.0)).isCloseTo(walk * 2.5, within(0.01));
        assertThat(PlaceRecommender.maxStraightDistance(45, MoveType.WALK, 0.7)).isCloseTo(walk * 0.7, within(0.01));
    }

    @Test
    void 편도_시간은_실제_길_기준으로_올림한다() {
        // 1000m × 1.3 = 1300m ÷ 67 = 19.4 → 20분
        assertThat(PlaceRecommender.oneWayMinutes(1000, MoveType.WALK)).isEqualTo(20);
        assertThat(PlaceRecommender.oneWayMinutes(1000, MoveType.BIKE)).isEqualTo(6);
    }

    @Test
    void 전체_시간은_왕복_이동과_장소에서_보내는_시간의_합() {
        assertThat(PlaceRecommender.totalMinutes(1000, MoveType.WALK, PlaceCategory.WALK)).isEqualTo(20 * 2 + 15);
    }

    @Test
    void 왕복_이동_거리는_직선거리에_우회계수와_2를_곱한다() {
        assertThat(PlaceRecommender.roundTripRouteMeters(1000)).isEqualTo(2600);
    }
}
