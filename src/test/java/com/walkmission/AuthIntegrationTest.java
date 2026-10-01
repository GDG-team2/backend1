package com.walkmission;

import com.walkmission.support.IntegrationTest;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/** 가입·로그인·토큰 흐름과 공통 에러 형식 */
class AuthIntegrationTest extends IntegrationTest {

    @Value("${jwt.secret-key}")
    private String jwtSecret;

    @Test
    void 가입하면_201과_약관_동의_기록이_남는다() throws Exception {
        String email = "auth-" + UUID.randomUUID() + "@test.local";
        Response res = post("/api/v1/auth/signup", null, signupRequest(email, uniqueRegion()));

        assertThat(res.status()).isEqualTo(201);
        Integer agreements = jdbc.queryForObject("""
                select count(*) from terms_agreement a join users u on u.id = a.user_id
                where u.email = ? and a.agreed = true""", Integer.class, email);
        assertThat(agreements).isEqualTo(3); // 필수 3개 동의, 마케팅은 미동의로 기록
    }

    @Test
    void 같은_이메일로_다시_가입하면_409() throws Exception {
        String email = "dup-" + UUID.randomUUID() + "@test.local";
        post("/api/v1/auth/signup", null, signupRequest(email, uniqueRegion()));
        Response res = post("/api/v1/auth/signup", null, signupRequest(email, uniqueRegion()));

        assertThat(res.status()).isEqualTo(409);
        assertThat(res.text("code")).isEqualTo("DUPLICATE_EMAIL");
    }

    @Test
    void 필수_약관을_빼면_빠진_약관을_알려준다() throws Exception {
        Response res = post("/api/v1/auth/signup", null, """
                {"email":"t-%s@test.local","password":"password123","nickname":"t","regionCode":"1174010800",
                 "agreements":{"service":true,"privacy":true,"location":false}}""".formatted(UUID.randomUUID()));

        assertThat(res.status()).isEqualTo(400);
        assertThat(res.text("code")).isEqualTo("TERMS_NOT_AGREED");
        assertThat(res.body().path("details").path("missing").get(0).asText()).isEqualTo("LOCATION");
    }

    @Test
    void 약한_비밀번호와_필수값_누락은_400() throws Exception {
        Response weak = post("/api/v1/auth/signup", null, """
                {"email":"w-%s@test.local","password":"short","nickname":"t","regionCode":"1174010800",
                 "agreements":{"service":true,"privacy":true,"location":true}}""".formatted(UUID.randomUUID()));
        assertThat(weak.text("code")).isEqualTo("INVALID_PASSWORD_FORMAT");

        Response missing = post("/api/v1/auth/signup", null, "{\"email\":\"not-an-email\"}");
        assertThat(missing.status()).isEqualTo(400);
        assertThat(missing.text("code")).isEqualTo("INVALID_INPUT");
        assertThat(missing.body().path("details").has("email")).isTrue();
    }

    @Test
    void 비밀번호가_틀리면_401() throws Exception {
        String email = "login-" + UUID.randomUUID() + "@test.local";
        post("/api/v1/auth/signup", null, signupRequest(email, uniqueRegion()));
        Response res = post("/api/v1/auth/login", null, "{\"email\":\"%s\",\"password\":\"wrong1234\"}".formatted(email));

        assertThat(res.status()).isEqualTo(401);
        assertThat(res.text("code")).isEqualTo("LOGIN_FAILED");
    }

    @Test
    void 토큰이_없거나_잘못되면_401() throws Exception {
        assertThat(get("/api/v1/users/profile", null).text("code")).isEqualTo("UNAUTHORIZED");
        assertThat(get("/api/v1/users/profile", "not.a.token").text("code")).isEqualTo("INVALID_TOKEN");
    }

    @Test
    void 만료된_토큰은_TOKEN_EXPIRED() throws Exception {
        String expired = Jwts.builder()
                .subject("1")
                .claim("type", "access")
                .expiration(new Date(System.currentTimeMillis() - 60_000))
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                .compact();

        Response res = get("/api/v1/users/profile", expired);
        assertThat(res.status()).isEqualTo(401);
        assertThat(res.text("code")).isEqualTo("TOKEN_EXPIRED");
    }

    @Test
    void 리프레시_토큰으로_재발급하고_리프레시_토큰은_API에_못_쓴다() throws Exception {
        String email = "re-" + UUID.randomUUID() + "@test.local";
        post("/api/v1/auth/signup", null, signupRequest(email, uniqueRegion()));
        Response login = post("/api/v1/auth/login", null, "{\"email\":\"%s\",\"password\":\"password123\"}".formatted(email));
        String access = login.text("accessToken");
        String refresh = login.text("refreshToken");

        assertThat(get("/api/v1/users/profile", refresh).text("code")).isEqualTo("INVALID_TOKEN");

        Response reissued = post("/api/v1/auth/reissue", null, "{\"refreshToken\":\"%s\"}".formatted(refresh));
        assertThat(reissued.status()).isEqualTo(200);
        assertThat(get("/api/v1/users/profile", reissued.text("accessToken")).status()).isEqualTo(200);

        Response wrongType = post("/api/v1/auth/reissue", null, "{\"refreshToken\":\"%s\"}".formatted(access));
        assertThat(wrongType.text("code")).isEqualTo("INVALID_REFRESH_TOKEN");
    }
}
