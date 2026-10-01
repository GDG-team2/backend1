package com.walkmission;

import com.walkmission.support.IntegrationTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

/** 같은 미션에 요청이 동시에 들어와도 정산은 한 번만 된다 (버튼 연타, 네트워크 재전송) */
class MissionConcurrencyIntegrationTest extends IntegrationTest {

    private static final int ROUNDS = 5;
    private static final int PARALLEL = 4;

    @Test
    void 완료를_동시에_여러_번_보내도_한_번만_정산한다() throws Exception {
        String token = newUser(uniqueRegion());

        for (int round = 1; round <= ROUNDS; round++) {
            long id = arrivedMission(token);
            List<Integer> statuses = fireTogether(() ->
                    post("/api/v1/missions/" + id + "/complete", token, "{\"afterSurveyScore\":4}").status());

            assertThat(statuses).filteredOn(s -> s == 200).hasSize(1);
            assertThat(statuses).filteredOn(s -> s == 409).hasSize(PARALLEL - 1);
            assertThat(currentPoint(token)).isEqualTo(50 * round);
        }
    }

    @Test
    void 완료와_중단이_동시에_와도_하나만_반영된다() throws Exception {
        String token = newUser(uniqueRegion());
        long id = arrivedMission(token);

        List<Integer> statuses = fireTogether(() -> post("/api/v1/missions/" + id + "/complete", token,
                "{\"afterSurveyScore\":4}").status(), () -> post("/api/v1/missions/" + id + "/abort", token, null).status());

        assertThat(statuses).filteredOn(s -> s == 200).hasSize(1);
        String status = get("/api/v1/missions/" + id, token).text("status");
        assertThat(currentPoint(token)).isEqualTo(status.equals("COMPLETED") ? 50 : 0);
    }

    private long arrivedMission(String token) throws Exception {
        Response rec = recommend(token, "\"category\":\"WALK\"");
        long id = rec.id("missionId");
        post("/api/v1/missions/" + id + "/start", token, null);
        post("/api/v1/missions/" + id + "/arrive", token, arriveBody(rec));
        skipDwell(id);
        post("/api/v1/missions/" + id + "/arrive", token, arriveBody(rec));
        return id;
    }

    private int currentPoint(String token) throws Exception {
        return get("/api/v1/users/profile", token).body().path("asset").path("currentPoint").asInt();
    }

    private List<Integer> fireTogether(Callable<Integer> request) throws Exception {
        List<Callable<Integer>> requests = new ArrayList<>();
        for (int i = 0; i < PARALLEL; i++) requests.add(request);
        return fireAll(requests);
    }

    @SafeVarargs
    private List<Integer> fireTogether(Callable<Integer>... requests) throws Exception {
        return fireAll(List.of(requests));
    }

    private List<Integer> fireAll(List<Callable<Integer>> requests) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(requests.size());
        CountDownLatch go = new CountDownLatch(1);
        try {
            List<Future<Integer>> futures = new ArrayList<>();
            for (Callable<Integer> request : requests) {
                futures.add(pool.submit(() -> {
                    go.await();
                    return request.call();
                }));
            }
            go.countDown();
            List<Integer> statuses = new ArrayList<>();
            for (Future<Integer> f : futures) statuses.add(f.get());
            return statuses;
        } finally {
            pool.shutdownNow();
        }
    }
}
