package com.walkmission.domain.mission.service;

import com.walkmission.domain.mission.entity.MoveType;
import com.walkmission.domain.mission.entity.PlaceCategory;
import com.walkmission.global.error.BusinessException;
import com.walkmission.global.error.ErrorCode;
import com.walkmission.global.external.kakao.KakaoLocalClient;
import com.walkmission.global.external.kakao.KakaoPlace;
import com.walkmission.global.util.GeoUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** 카카오 검색 결과를 가짜로 주고, 거리 범위·분류 필터·제외 목록이 지켜지는지 본다 */
class PlaceRecommenderTest {
    private static final double LAT = 37.498095;
    private static final double LON = 127.027610;

    private KakaoLocalClient kakao;
    private PlaceRecommender recommender;

    @BeforeEach
    void setUp() {
        kakao = mock(KakaoLocalClient.class);
        recommender = new PlaceRecommender(kakao);
    }

    private static KakaoPlace place(String id, String categoryName, double lat, double lon) {
        return new KakaoPlace(id, "장소" + id, categoryName, "도로명", "지번", "http://place.map.kakao.com/" + id,
                String.valueOf(lon), String.valueOf(lat));
    }

    private PlaceRecommender.Request request(List<PlaceCategory> categories, Set<String> excluded) {
        return new PlaceRecommender.Request(LAT, LON, 60, MoveType.WALK, categories, 1.0, excluded);
    }

    @Test
    void 검색_중심점의_장소를_추천하고_거리는_추천_범위_안이다() {
        // 검색 중심(사용자에게서 목표 거리만큼 떨어진 지점)에 있는 공원을 돌려준다
        when(kakao.searchByKeyword(eq("공원"), anyDouble(), anyDouble(), anyInt()))
                .thenAnswer(inv -> List.of(place("p1", "여행 > 공원 > 도시근린공원", inv.getArgument(1), inv.getArgument(2))));

        PlaceRecommender.Candidate c = recommender.recommend(request(List.of(PlaceCategory.WALK), Set.of()));

        double max = PlaceRecommender.maxStraightDistance(45, MoveType.WALK, 1.0);
        assertThat(c.place().id()).isEqualTo("p1");
        assertThat(c.category()).isEqualTo(PlaceCategory.WALK);
        assertThat(c.distanceMeters()).isBetween((int) (max * 0.6), (int) Math.ceil(max));
    }

    @Test
    void 공원_주차장처럼_분류가_맞지_않는_결과는_거른다() {
        when(kakao.searchByKeyword(eq("공원"), anyDouble(), anyDouble(), anyInt()))
                .thenAnswer(inv -> List.of(place("parking", "교통,수송 > 주차장 > 공영주차장", inv.getArgument(1), inv.getArgument(2))));

        assertThatThrownBy(() -> recommender.recommend(request(List.of(PlaceCategory.WALK), Set.of())))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.NO_NEARBY_PLACE);
    }

    @Test
    void 제외_목록에_있는_장소는_추천하지_않는다() {
        when(kakao.searchByCategory(eq("CE7"), anyDouble(), anyDouble(), anyInt()))
                .thenAnswer(inv -> List.of(place("visited", "음식점 > 카페", inv.getArgument(1), inv.getArgument(2))));

        assertThatThrownBy(() -> recommender.recommend(request(List.of(PlaceCategory.CAFE), Set.of("visited"))))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void 너무_가까운_곳만_있으면_범위를_넓혀_찾되_최소_거리를_지킨다() {
        double max = PlaceRecommender.maxStraightDistance(45, MoveType.WALK, 1.0);
        double[] tooClose = GeoUtils.destination(LAT, LON, 0, 20);            // 20m: 산책이 안 되는 거리
        double[] fallback = GeoUtils.destination(LAT, LON, 90, max * 0.4);   // 넓힌 범위의 최소 거리(30%) 이상
        when(kakao.searchByKeyword(eq("공원"), anyDouble(), anyDouble(), anyInt())).thenReturn(List.of(
                place("near", "여행 > 공원", tooClose[0], tooClose[1]),
                place("ok", "여행 > 공원", fallback[0], fallback[1])));

        PlaceRecommender.Candidate c = recommender.recommend(request(List.of(PlaceCategory.WALK), Set.of()));

        assertThat(c.place().id()).isEqualTo("ok");
    }

    @Test
    void 앞에_둔_범주부터_시도한다() {
        when(kakao.searchByCategory(eq("CE7"), anyDouble(), anyDouble(), anyInt()))
                .thenAnswer(inv -> List.of(place("cafe", "음식점 > 카페", inv.getArgument(1), inv.getArgument(2))));
        when(kakao.searchByKeyword(eq("공원"), anyDouble(), anyDouble(), anyInt()))
                .thenAnswer(inv -> List.of(place("park", "여행 > 공원", inv.getArgument(1), inv.getArgument(2))));

        assertThat(recommender.recommend(request(List.of(PlaceCategory.CAFE, PlaceCategory.WALK), Set.of())).category())
                .isEqualTo(PlaceCategory.CAFE);
        assertThat(recommender.recommend(request(List.of(PlaceCategory.WALK, PlaceCategory.CAFE), Set.of())).category())
                .isEqualTo(PlaceCategory.WALK);
    }
}
