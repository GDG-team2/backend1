package com.walkmission.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.walkmission.global.external.kakao.KakaoLocalClient;
import com.walkmission.global.external.kakao.KakaoPlace;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * 실제 서버(스프링)와 테스트 DB로 API를 호출하는 테스트의 공통 기반.
 * - 카카오 장소 검색은 가짜: 검색 중심점에 매번 새 장소 하나를 돌려준다(항상 추천 범위 안).
 * - 테스트끼리 섞이지 않도록 사용자·동네 코드를 매번 새로 만든다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"local", "test"})
public abstract class IntegrationTest {
    protected static final double LAT = 37.498095;
    protected static final double LON = 127.027610;
    private static final AtomicInteger PLACE_SEQ = new AtomicInteger();

    @Autowired protected MockMvc mvc;
    @Autowired protected ObjectMapper objectMapper;
    @Autowired protected JdbcTemplate jdbc;
    @MockBean protected KakaoLocalClient kakaoLocalClient;

    @BeforeEach
    void fakeKakao() {
        when(kakaoLocalClient.searchByCategory(anyString(), anyDouble(), anyDouble(), anyInt()))
                .thenAnswer(inv -> List.of(fakePlace(categoryNameForCode(inv.getArgument(0)), inv.getArgument(1), inv.getArgument(2))));
        when(kakaoLocalClient.searchByKeyword(anyString(), anyDouble(), anyDouble(), anyInt()))
                .thenAnswer(inv -> List.of(fakePlace(categoryNameForKeyword(inv.getArgument(0)), inv.getArgument(1), inv.getArgument(2))));
    }

    private static KakaoPlace fakePlace(String categoryName, double lat, double lon) {
        String id = "fake-" + PLACE_SEQ.incrementAndGet();
        return new KakaoPlace(id, "테스트장소" + id, categoryName, "서울 강남구 테스트로 1", null,
                "http://place.map.kakao.com/" + id, String.valueOf(lon), String.valueOf(lat));
    }

    private static String categoryNameForCode(String code) {
        return switch (code) {
            case "CE7" -> "음식점 > 카페";
            case "AT4" -> "여행 > 관광,명소 > 테마거리";
            case "FD6" -> "음식점 > 한식";
            default -> "기타";
        };
    }

    private static String categoryNameForKeyword(String keyword) {
        return keyword.equals("공원") ? "여행 > 공원 > 도시근린공원" : "문화,예술 > 문화시설 > 미술관";
    }

    // ---------- HTTP 헬퍼 ----------

    protected record Response(int status, JsonNode body) {
        public String text(String field) { return body.path(field).asText(); }
        public long id(String field) { return body.path(field).asLong(); }
    }

    protected Response call(MockHttpServletRequestBuilder request, String token, Object body) throws Exception {
        request.contentType(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8);
        if (token != null) request.header("Authorization", "Bearer " + token);
        if (body != null) request.content(body instanceof String s ? s : objectMapper.writeValueAsString(body));
        MvcResult result = mvc.perform(request).andReturn();
        String content = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        return new Response(result.getResponse().getStatus(),
                content.isBlank() ? objectMapper.createObjectNode() : objectMapper.readTree(content));
    }

    protected Response get(String path, String token) throws Exception { return call(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(path), token, null); }
    protected Response post(String path, String token, Object body) throws Exception { return call(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(path), token, body); }
    protected Response put(String path, String token, Object body) throws Exception { return call(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put(path), token, body); }
    protected Response patch(String path, String token, Object body) throws Exception { return call(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch(path), token, body); }

    // ---------- 시나리오 헬퍼 ----------

    protected static String uniqueRegion() {
        return "T" + UUID.randomUUID().toString().substring(0, 8);
    }

    protected String signupRequest(String email, String region) {
        return """
                {"email":"%s","password":"password123","nickname":"tester","regionCode":"%s",
                 "agreements":{"service":true,"privacy":true,"location":true}}""".formatted(email, region);
    }

    /** 새 사용자를 만들고 액세스 토큰을 돌려준다 */
    protected String newUser(String region) throws Exception {
        String email = "it-" + UUID.randomUUID() + "@test.local";
        Response signup = post("/api/v1/auth/signup", null, signupRequest(email, region));
        if (signup.status() != 201) throw new IllegalStateException("signup failed: " + signup.body());
        return login(email);
    }

    protected String login(String email) throws Exception {
        return post("/api/v1/auth/login", null, """
                {"email":"%s","password":"password123"}""".formatted(email)).text("accessToken");
    }

    protected Response recommend(String token, String extraJson) throws Exception {
        String body = "{\"latitude\":%s,\"longitude\":%s%s}".formatted(LAT, LON,
                extraJson == null || extraJson.isBlank() ? "" : "," + extraJson);
        return post("/api/v1/missions/recommendation", token, body);
    }

    /** 도착 반경 체류 30초를 기다리지 않도록 체류 시작 시각을 앞당긴다 */
    protected void skipDwell(long missionId) {
        jdbc.update("update mission_record set arrival_check_started_at = now() - interval '31 seconds' where id = ?", missionId);
    }

    protected String arriveBody(Response recommendation) {
        JsonNode place = recommendation.body().path("place");
        return "{\"latitude\":%s,\"longitude\":%s}".formatted(place.path("latitude").asText(), place.path("longitude").asText());
    }

    /** 추천부터 완료까지 한 번에 진행하고 완료 응답을 돌려준다 */
    protected Response completeMission(String token, String category, int satisfaction) throws Exception {
        Response rec = recommend(token, "\"category\":\"" + category + "\"");
        long id = rec.id("missionId");
        post("/api/v1/missions/" + id + "/start", token, null);
        post("/api/v1/missions/" + id + "/arrive", token, arriveBody(rec));
        skipDwell(id);
        post("/api/v1/missions/" + id + "/arrive", token, arriveBody(rec));
        return post("/api/v1/missions/" + id + "/complete", token, "{\"afterSurveyScore\":" + satisfaction + "}");
    }
}
