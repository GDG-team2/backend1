package com.walkmission.global.health;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** 호스팅(Render)이 서버가 살아 있는지 확인하는 주소. 인증 없이 열려 있다(/api/v1 밖). */
@Hidden
@RestController
public class HealthController {

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }
}
