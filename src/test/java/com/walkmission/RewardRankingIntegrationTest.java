package com.walkmission;

import com.walkmission.support.IntegrationTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** 완료 정산: 포인트, 주간 리듬, 랭킹 점수(주 3회·다양성 보너스), 배지 */
class RewardRankingIntegrationTest extends IntegrationTest {

    @Test
    void 랭킹은_주_3회까지만_반영하고_그_주에_처음_해본_범주면_20점을_더한다() throws Exception {
        String region = uniqueRegion();
        String token = newUser(region);

        List<Response> results = new ArrayList<>();
        for (String category : List.of("WALK", "WALK", "CAFE", "FOOD")) {
            results.add(completeMission(token, category, 4));
        }

        assertThat(results).extracting(r -> r.body().path("ranking").path("earnedScore").asInt())
                .containsExactly(120, 100, 120, 0);
        assertThat(results.get(3).body().path("ranking").path("currentWeeklyScore").asInt()).isEqualTo(340);
        assertThat(results.get(3).body().path("reward").path("currentTotalPoint").asInt()).isEqualTo(200); // 포인트는 제한 없음

        Response ranking = get("/api/v1/rankings/my-region", token);
        assertThat(ranking.body().path("myRanking").path("rank").asInt()).isEqualTo(1);
        assertThat(ranking.body().path("myRanking").path("scoredMissionCount").asInt()).isEqualTo(3);
        assertThat(ranking.body().path("myRanking").path("bonusScore").asInt()).isEqualTo(40);
    }

    @Test
    void 주간_목표를_채우면_리듬이_시작되고_첫_리듬_배지를_준다() throws Exception {
        String token = newUser(uniqueRegion());

        Response first = completeMission(token, "WALK", 4);
        Response second = completeMission(token, "WALK", 4);
        Response third = completeMission(token, "CAFE", 4);

        assertThat(first.body().path("rhythm").path("goalJustAchieved").asBoolean()).isFalse();
        assertThat(second.body().path("rhythm").path("thisWeekCount").asInt()).isEqualTo(2);
        assertThat(third.body().path("rhythm").path("goalJustAchieved").asBoolean()).isTrue();
        assertThat(third.body().path("rhythm").path("currentWeeks").asInt()).isEqualTo(1);
        assertThat(third.body().path("newBadges").findValuesAsText("badgeName")).contains("첫 리듬");

        Response profile = get("/api/v1/users/profile", token);
        assertThat(profile.body().path("rhythm").path("goalAchievedThisWeek").asBoolean()).isTrue();
        assertThat(profile.body().path("stats").path("totalCompletedMissions").asInt()).isEqualTo(3);
    }

    @Test
    void 지난주까지_3주_연속이면_이번_주_달성으로_4주가_되고_리듬_메이커_배지를_준다() throws Exception {
        String token = newUser(uniqueRegion());
        long userId = currentUserId(token);
        jdbc.update("""
                update user_profile set rhythm_weeks = 3, best_rhythm_weeks = 3,
                       last_rhythm_week_start = date_trunc('week', now())::date - 7
                where user_id = ?""", userId);
        patch("/api/v1/users/preferences", token, "{\"weeklyGoal\":1}");

        Response done = completeMission(token, "WALK", 5);

        assertThat(done.body().path("rhythm").path("currentWeeks").asInt()).isEqualTo(4);
        assertThat(done.body().path("newBadges").findValuesAsText("badgeName")).contains("리듬 메이커");
    }

    @Test
    void 랭킹_공개를_끄면_리더보드에_없고_내_순위도_비어_있다() throws Exception {
        String region = uniqueRegion();
        String visible = newUser(region);
        String hidden = newUser(region);
        patch("/api/v1/users/settings", hidden, "{\"rankingSetting\":false}");
        completeMission(visible, "WALK", 4);
        Response hiddenDone = completeMission(hidden, "WALK", 4);

        assertThat(hiddenDone.body().path("ranking").path("isParticipant").asBoolean()).isFalse();

        Response board = get("/api/v1/rankings/my-region", hidden);
        assertThat(board.body().path("leaderboard").size()).isEqualTo(1);
        assertThat(board.body().path("myRanking").path("rank").isNull()).isTrue();
        assertThat(board.body().path("myRanking").path("score").asInt()).isEqualTo(120);
    }

    @Test
    void 배지_도감에_진행도가_나온다() throws Exception {
        String token = newUser(uniqueRegion());
        completeMission(token, "WALK", 4);
        completeMission(token, "CAFE", 4);

        Response badges = get("/api/v1/rewards/badges", token);
        assertThat(badges.body().path("summary").path("totalCount").asInt()).isEqualTo(5);
        badges.body().path("badges").forEach(b -> {
            if (b.path("badgeName").asText().equals("꼼지락 습관")) {
                assertThat(b.path("progressCurrent").asInt()).isEqualTo(2);
                assertThat(b.path("progressTarget").asInt()).isEqualTo(10);
            }
        });
    }

    private long currentUserId(String token) throws Exception {
        String uuid = get("/api/v1/users/profile", token).text("userUuid");
        return jdbc.queryForObject("select id from users where user_uuid = ?::uuid", Long.class, uuid);
    }
}
