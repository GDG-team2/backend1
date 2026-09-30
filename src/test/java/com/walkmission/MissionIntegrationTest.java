package com.walkmission;

import com.walkmission.support.IntegrationTest;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

/** 미션 흐름: 추천 → (약속) → 출발 → 도착(80m + 30초) → 완료, 그리고 순서·권한 검사 */
class MissionIntegrationTest extends IntegrationTest {

    private static String kst(LocalDateTime t) {
        return t.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    @Test
    void 추천부터_완료까지_정상_흐름() throws Exception {
        String token = newUser(uniqueRegion());

        Response rec = recommend(token, "\"mood\":\"GLOOMY\",\"category\":\"WALK\"");
        assertThat(rec.status()).isEqualTo(200);
        assertThat(rec.text("status")).isEqualTo("READY");
        assertThat(rec.text("missionTitle")).endsWith("한 바퀴");
        assertThat(rec.text("reason")).isNotBlank();
        assertThat(rec.text("placeCategory")).isEqualTo("WALK");
        assertThat(rec.body().path("totalMinutes").asInt()).isLessThanOrEqualTo(60 + 5);
        assertThat(rec.body().path("isNewPlace").asBoolean()).isTrue();
        long id = rec.id("missionId");

        assertThat(post("/api/v1/missions/" + id + "/start", token, null).text("status")).isEqualTo("IN_PROGRESS");

        Response firstArrive = post("/api/v1/missions/" + id + "/arrive", token, arriveBody(rec));
        assertThat(firstArrive.text("status")).isEqualTo("IN_PROGRESS");
        assertThat(firstArrive.body().path("remainingDwellSeconds").asInt()).isEqualTo(30);

        skipDwell(id);
        Response arrived = post("/api/v1/missions/" + id + "/arrive", token, arriveBody(rec));
        assertThat(arrived.text("status")).isEqualTo("ARRIVED");
        assertThat(arrived.body().path("remainingDwellSeconds").asInt()).isZero();

        Response done = post("/api/v1/missions/" + id + "/complete", token, "{\"afterSurveyScore\":5,\"stepCount\":3200}");
        assertThat(done.status()).isEqualTo(200);
        assertThat(done.text("status")).isEqualTo("COMPLETED");
        assertThat(done.body().path("reward").path("earnedPoint").asInt()).isEqualTo(50);
        assertThat(done.body().path("isNewPlace").asBoolean()).isTrue();
        assertThat(done.body().path("newBadges").get(0).path("badgeName").asText()).isEqualTo("첫 꼼지락");

        Response detail = get("/api/v1/missions/" + id, token);
        assertThat(detail.body().path("mood").path("beforeScore").asInt()).isEqualTo(2);   // 꿀꿀함
        assertThat(detail.body().path("mood").path("change").asInt()).isEqualTo(3);        // 2 → 5
    }

    @Test
    void 도착_반경_80m_밖이면_남은_거리와_함께_400() throws Exception {
        String token = newUser(uniqueRegion());
        long id = recommend(token, null).id("missionId");
        post("/api/v1/missions/" + id + "/start", token, null);

        Response far = post("/api/v1/missions/" + id + "/arrive", token, "{\"latitude\":37.60,\"longitude\":127.10}");
        assertThat(far.status()).isEqualTo(400);
        assertThat(far.text("code")).isEqualTo("NOT_ENOUGH_DISTANCE");
        assertThat(far.body().path("details").path("currentDistanceMeters").asInt()).isGreaterThan(80);
    }

    @Test
    void 순서가_틀리면_409와_현재_상태를_알려준다() throws Exception {
        String token = newUser(uniqueRegion());
        long id = recommend(token, null).id("missionId");

        Response res = post("/api/v1/missions/" + id + "/complete", token, "{\"afterSurveyScore\":5}");
        assertThat(res.status()).isEqualTo(409);
        assertThat(res.text("code")).isEqualTo("INVALID_MISSION_STATUS");
        assertThat(res.body().path("details").path("currentStatus").asText()).isEqualTo("READY");
    }

    @Test
    void 남의_미션은_404() throws Exception {
        String owner = newUser(uniqueRegion());
        String other = newUser(uniqueRegion());
        long id = recommend(owner, null).id("missionId");

        Response res = post("/api/v1/missions/" + id + "/start", other, null);
        assertThat(res.status()).isEqualTo(404);
        assertThat(res.text("code")).isEqualTo("MISSION_NOT_FOUND");
    }

    @Test
    void 산책_중에는_새_추천을_받을_수_없다() throws Exception {
        String token = newUser(uniqueRegion());
        long id = recommend(token, null).id("missionId");
        post("/api/v1/missions/" + id + "/start", token, null);

        Response res = recommend(token, null);
        assertThat(res.status()).isEqualTo(409);
        assertThat(res.text("code")).isEqualTo("ACTIVE_MISSION_EXISTS");
    }

    @Test
    void 다시_추천하면_약속하지_않은_추천만_대체되고_거절_이유가_남는다() throws Exception {
        String token = newUser(uniqueRegion());
        long scheduled = recommend(token, null).id("missionId");
        Response sch = put("/api/v1/missions/" + scheduled + "/schedule", token,
                "{\"departAt\":\"" + kst(LocalDateTime.now().plusMinutes(10)) + "\"}");
        assertThat(sch.status()).isEqualTo(200);
        assertThat(sch.body().path("minutesUntilDeparture").asInt()).isEqualTo(10);

        long unscheduled = recommend(token, null).id("missionId");
        long replacement = recommend(token, "\"rejectReason\":\"TOO_FAR\"").id("missionId");

        assertThat(statusOf(scheduled)).isEqualTo("READY");
        assertThat(statusOf(unscheduled)).isEqualTo("ABORTED");
        assertThat(jdbc.queryForObject("select reject_reason from mission_record where id = ?", String.class, unscheduled))
                .isEqualTo("TOO_FAR");
        assertThat(statusOf(replacement)).isEqualTo("READY");

        assertThat(get("/api/v1/missions/scheduled", token).body().path("missions").size()).isEqualTo(1);
        // 앱 복구는 약속하지 않은 최신 추천을 먼저 돌려준다
        assertThat(get("/api/v1/missions/current", token).body().path("mission").path("missionId").asLong())
                .isEqualTo(replacement);
    }

    @Test
    void 지난_시각이나_14일_뒤로는_약속할_수_없다() throws Exception {
        String token = newUser(uniqueRegion());
        long id = recommend(token, null).id("missionId");

        assertThat(put("/api/v1/missions/" + id + "/schedule", token,
                "{\"departAt\":\"" + kst(LocalDateTime.now().minusHours(1)) + "\"}").status()).isEqualTo(400);
        assertThat(put("/api/v1/missions/" + id + "/schedule", token,
                "{\"departAt\":\"" + kst(LocalDateTime.now().plusDays(15)) + "\"}").status()).isEqualTo(400);
    }

    @Test
    void 예산을_넘는_범주만_남으면_이유와_함께_404() throws Exception {
        String token = newUser(uniqueRegion());
        Response res = recommend(token, "\"budget\":\"FREE\",\"category\":\"CAFE\"");

        assertThat(res.status()).isEqualTo(404);
        assertThat(res.text("code")).isEqualTo("NO_NEARBY_PLACE");
        assertThat(res.body().path("details").path("reason").asText()).isEqualTo("BUDGET");
    }

    @Test
    void 무료_예산이면_산책이나_구경만_추천한다() throws Exception {
        String token = newUser(uniqueRegion());
        for (int i = 0; i < 4; i++) {
            Response res = recommend(token, "\"budget\":\"FREE\"");
            assertThat(res.text("placeCategory")).isIn("WALK", "SIGHTSEEING");
            assertThat(res.body().path("estCost").asInt()).isZero();
        }
    }

    @Test
    void 포기하면_이유와_거리가_남고_피곤하면_다음_추천_안내를_준다() throws Exception {
        String token = newUser(uniqueRegion());
        long id = recommend(token, null).id("missionId");
        post("/api/v1/missions/" + id + "/start", token, null);

        Response res = post("/api/v1/missions/" + id + "/abort", token, "{\"reason\":\"TIRED\",\"movedDistanceMeters\":700}");
        assertThat(res.text("status")).isEqualTo("ABORTED");
        assertThat(res.body().path("movedDistanceMeters").asInt()).isEqualTo(700);
        assertThat(res.text("nextRecommendationNote")).isNotBlank();

        Response detail = get("/api/v1/missions/" + id, token);
        assertThat(detail.body().path("abort").path("reason").asText()).isEqualTo("TIRED");
    }

    private String statusOf(long missionId) {
        return jdbc.queryForObject("select status from mission_record where id = ?", String.class, missionId);
    }
}
