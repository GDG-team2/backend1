package com.walkmission.domain.user.controller;

import com.walkmission.domain.user.dto.PreferenceResponse;
import com.walkmission.domain.user.dto.PreferenceUpdateRequest;
import com.walkmission.domain.user.dto.UserProfileResponse;
import com.walkmission.domain.user.dto.UserSettingsUpdateRequest;
import com.walkmission.domain.user.dto.UserSettingsUpdateResponse;
import com.walkmission.domain.user.service.UserService;
import com.walkmission.global.auth.LoginUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User & Auth Domain")
@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    
    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "사용자 프로필 조회")
    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getProfile(@LoginUser Long userId) {
        return ResponseEntity.ok(userService.getProfile(userId));
    }

    @Operation(summary = "사용자 환경설정 수정")
    @PatchMapping("/settings")
    public ResponseEntity<UserSettingsUpdateResponse> updateSettings(
            @LoginUser Long userId,
            @Valid @RequestBody UserSettingsUpdateRequest request) {
        return ResponseEntity.ok(userService.updateSettings(userId, request));
    }

    @Operation(summary = "미션 선호 설정 조회", description = "희망 산책 시간, 이동수단, 선호 범주를 조회합니다. 범주를 고르지 않았으면 전체 범주가 반환됩니다.")
    @GetMapping("/preferences")
    public ResponseEntity<PreferenceResponse> getPreference(@LoginUser Long userId) {
        return ResponseEntity.ok(userService.getPreference(userId));
    }

    @Operation(summary = "미션 선호 설정 변경", description = "보낸 필드만 변경합니다. 미션 추천 시 이 설정이 기본값으로 쓰입니다.")
    @PatchMapping("/preferences")
    public ResponseEntity<PreferenceResponse> updatePreference(
            @LoginUser Long userId,
            @Valid @RequestBody PreferenceUpdateRequest request) {
        return ResponseEntity.ok(userService.updatePreference(userId, request));
    }
}
