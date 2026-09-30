package com.walkmission;

import com.walkmission.support.IntegrationTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** 기록 조회, 내 지도, 프로필·선호·설정 */
class HistoryProfileIntegrationTest extends IntegrationTest {

    @Test
    void 활동_기록은_기간과_범주와_무료_필터로_거르고_요약을_준다() throws Exception {
        String token = newUser(uniqueRegion());
        completeMission(token, "WALK", 5);
        completeMission(token, "WALK", 3);
        completeMission(token, "CAFE", 4);

        Response all = get("/api/v1/missions/history?period=WEEK", token);
        assertThat(all.body().path("summary").path("completedCount").asInt()).isEqualTo(3);
        assertThat(all.body().path("summary").path("averageSatisfaction").asDouble()).isEqualTo(4.0);
        assertThat(all.body().path("records").size()).isEqualTo(3);

        assertThat(get("/api/v1/missions/history?period=ALL&category=WALK", token)
                .body().path("summary").path("completedCount").asInt()).isEqualTo(2);
        assertThat(get("/api/v1/missions/history?period=ALL&freeOnly=true", token)
                .body().path("summary").path("completedCount").asInt()).isEqualTo(2);
        assertThat(get("/api/v1/missions/history?period=ALL&category=CAFE&freeOnly=true", token)
                .body().path("records").size()).isZero();

        Response paged = get("/api/v1/missions/history?period=ALL&size=2", token);
        assertThat(paged.body().path("records").size()).isEqualTo(2);
        assertThat(paged.body().path("pagination").path("hasNext").asBoolean()).isTrue();
    }

    @Test
    void 잘못된_기간_값은_400() throws Exception {
        String token = newUser(uniqueRegion());
        assertThat(get("/api/v1/missions/history?yearMonth=2026-13", token).text("code")).isEqualTo("INVALID_INPUT");
        assertThat(get("/api/v1/missions/history?period=YEAR", token).text("code")).isEqualTo("INVALID_INPUT");
    }

    @Test
    void 내_지도는_다녀온_장소와_통계를_준다() throws Exception {
        String token = newUser(uniqueRegion());
        completeMission(token, "WALK", 4);
        completeMission(token, "SIGHTSEEING", 4);

        Response map = get("/api/v1/missions/map", token);
        assertThat(map.body().path("stats").path("outingCount").asInt()).isEqualTo(2);
        assertThat(map.body().path("stats").path("newPlaceCount").asInt()).isEqualTo(2);
        assertThat(map.body().path("places").size()).isEqualTo(2);
        assertThat(map.body().path("recent").size()).isEqualTo(2);
    }

    @Test
    void 프로필을_고치면_랭킹에는_랭킹용_닉네임이_보인다() throws Exception {
        String region = uniqueRegion();
        String token = newUser(region);
        completeMission(token, "WALK", 4);

        Response updated = patch("/api/v1/users/profile", token,
                "{\"nickname\":\"선우\",\"rankingNickname\":\"산책왕\",\"birthYear\":2006}");
        assertThat(updated.text("nickname")).isEqualTo("선우");
        assertThat(updated.body().path("birthYear").asInt()).isEqualTo(2006);
        assertThat(get("/api/v1/rankings/my-region", token).body().path("myRanking").path("nickname").asText())
                .isEqualTo("산책왕");

        patch("/api/v1/users/profile", token, "{\"rankingNickname\":\"\"}");
        assertThat(get("/api/v1/rankings/my-region", token).body().path("myRanking").path("nickname").asText())
                .isEqualTo("선우");
    }

    @Test
    void 공백_닉네임과_미래_출생연도는_거절한다() throws Exception {
        String token = newUser(uniqueRegion());
        assertThat(patch("/api/v1/users/profile", token, "{\"nickname\":\"   \"}").text("code")).isEqualTo("INVALID_INPUT");
        assertThat(patch("/api/v1/users/profile", token, "{\"birthYear\":2999}").text("code")).isEqualTo("INVALID_INPUT");
    }

    @Test
    void 선호_설정_기본값과_범위_검사() throws Exception {
        String token = newUser(uniqueRegion());
        Response prefs = get("/api/v1/users/preferences", token);
        assertThat(prefs.body().path("walkTime").asInt()).isEqualTo(60);
        assertThat(prefs.body().path("weeklyGoal").asInt()).isEqualTo(3);
        assertThat(prefs.text("budget")).isEqualTo("ANY");
        assertThat(prefs.body().path("categories").size()).isEqualTo(5);

        Response changed = patch("/api/v1/users/preferences", token,
                "{\"walkTime\":90,\"budget\":\"UNDER_10K\",\"categories\":[\"WALK\"]}");
        assertThat(changed.body().path("walkTime").asInt()).isEqualTo(90);
        assertThat(changed.text("budget")).isEqualTo("UNDER_10K");
        assertThat(changed.body().path("categories").size()).isEqualTo(1);

        assertThat(patch("/api/v1/users/preferences", token, "{\"weeklyGoal\":9}").status()).isEqualTo(400);
        assertThat(patch("/api/v1/users/preferences", token, "{\"moveType\":\"CAR\"}").status()).isEqualTo(400);
    }

    @Test
    void 조용한_시간은_시분만_보내도_저장되고_초까지_돌려준다() throws Exception {
        String token = newUser(uniqueRegion());
        Response res = patch("/api/v1/users/settings", token,
                "{\"quietEnabled\":true,\"quietStart\":\"22:00\",\"quietEnd\":\"09:00:00\"}");

        assertThat(res.body().path("quietEnabled").asBoolean()).isTrue();
        assertThat(res.text("quietStart")).isEqualTo("22:00:00");
        assertThat(get("/api/v1/users/settings", token).text("quietEnd")).isEqualTo("09:00:00");
    }
}
