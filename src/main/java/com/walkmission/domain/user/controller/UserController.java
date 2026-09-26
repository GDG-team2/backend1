package com.walkmission.domain.user.controller;

import com.walkmission.domain.user.dto.UserProfileResponse;
import com.walkmission.domain.user.dto.UserSettingsUpdateRequest;
import com.walkmission.domain.user.dto.UserSettingsUpdateResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Tag(name = "User & Auth Domain", description = "인증 및 사용자 관련 API")
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Operation(summary = "마이페이지 프로필/자산 조회", description = "사용자의 프로필, 지역, 자산, 스트릭, 배지 및 통계를 조회합니다.")
    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getProfile(@RequestHeader("Authorization") String token) {
        UserProfileResponse response = new UserProfileResponse(
                UUID.randomUUID(),
                "user@example.com",
                "walking_master",
                new UserProfileResponse.RegionInfo("1168010100", "서울특별시 강남구 역삼동"),
                new UserProfileResponse.AssetInfo(1500),
                new UserProfileResponse.StreakInfo(5, 14),
                new UserProfileResponse.BadgeInfo(1L, "첫 산책 마스터", "https://example.com/badges/1.png"),
                new UserProfileResponse.StatsInfo(42)
        );
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "알림 및 공개 설정 변경", description = "사용자의 앱 내 알림 및 개인정보 공개 설정을 변경합니다.")
    @PatchMapping("/settings")
    public ResponseEntity<UserSettingsUpdateResponse> updateSettings(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody UserSettingsUpdateRequest request) {
        
        UserSettingsUpdateResponse response = new UserSettingsUpdateResponse(
                UUID.randomUUID(),
                request.allAlarm() != null ? request.allAlarm() : true,
                request.startAlarm() != null ? request.startAlarm() : true,
                request.missionAlarm() != null ? request.missionAlarm() : true,
                request.insightAlarm() != null ? request.insightAlarm() : true,
                request.rewardAlarm() != null ? request.rewardAlarm() : true,
                request.quietStart() != null ? request.quietStart() : LocalTime.of(22, 0),
                request.quietEnd() != null ? request.quietEnd() : LocalTime.of(7, 0),
                request.rankingSetting() != null ? request.rankingSetting() : true,
                request.nameSetting() != null ? request.nameSetting() : true,
                request.placeSetting() != null ? request.placeSetting() : true,
                request.friendSetting() != null ? request.friendSetting() : true,
                LocalDateTime.now(),
                "설정이 성공적으로 변경되었습니다."
        );
        return ResponseEntity.ok(response);
    }
}
