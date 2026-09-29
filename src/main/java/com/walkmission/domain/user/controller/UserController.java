package com.walkmission.domain.user.controller;

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
}
