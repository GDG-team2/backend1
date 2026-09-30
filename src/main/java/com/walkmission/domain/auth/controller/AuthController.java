package com.walkmission.domain.auth.controller;

import com.walkmission.domain.auth.dto.LoginRequest;
import com.walkmission.domain.auth.dto.LoginResponse;
import com.walkmission.domain.auth.dto.ReissueRequest;
import com.walkmission.domain.auth.dto.ReissueResponse;
import com.walkmission.domain.auth.dto.SignupRequest;
import com.walkmission.domain.auth.dto.SignupResponse;
import com.walkmission.domain.user.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User & Auth Domain", description = "인증 및 사용자 관련 API")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    
    private final AuthService authService;
    
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
    @ApiResponse(responseCode = "201", description = "가입 완료")
    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
        SignupResponse response = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "로그인 및 토큰 발급", description = "사용자 로그인을 처리하고 액세스 토큰을 발급합니다.")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "토큰 재발급", description = "리프레시 토큰을 검증하고 새로운 액세스 토큰과 리프레시 토큰을 발급합니다.")
    @PostMapping("/reissue")
    public ResponseEntity<ReissueResponse> reissue(@Valid @RequestBody ReissueRequest request) {
        ReissueResponse response = authService.reissue(request);
        return ResponseEntity.ok(response);
    }
}
