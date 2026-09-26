package com.walkmission.domain.auth.controller;

import com.walkmission.domain.auth.dto.LoginRequest;
import com.walkmission.domain.auth.dto.LoginResponse;
import com.walkmission.domain.auth.dto.SignupRequest;
import com.walkmission.domain.auth.dto.SignupResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "User & Auth Domain", description = "인증 및 사용자 관련 API")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
        SignupResponse response = new SignupResponse(
                UUID.randomUUID(),
                request.nickname(),
                "회원가입이 성공적으로 완료되었습니다."
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "로그인 및 토큰 발급", description = "사용자 로그인을 처리하고 액세스 토큰을 발급합니다.")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = new LoginResponse(
                "mock-access-token-string",
                "mock-refresh-token-string",
                UUID.randomUUID(),
                "walking_master"
        );
        return ResponseEntity.ok(response);
    }
}
