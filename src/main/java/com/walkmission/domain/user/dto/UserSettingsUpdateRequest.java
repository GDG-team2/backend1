package com.walkmission.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalTime;

@Schema(description = "알림 및 공개 설정 변경 요청 DTO")
public record UserSettingsUpdateRequest(
        @Schema(description = "전체 알림", example = "true") Boolean allAlarm,
        @Schema(description = "시작 알림", example = "true") Boolean startAlarm,
        @Schema(description = "미션 알림", example = "true") Boolean missionAlarm,
        @Schema(description = "인사이트 알림", example = "true") Boolean insightAlarm,
        @Schema(description = "보상 알림", example = "true") Boolean rewardAlarm,
        
        @Schema(description = "방해 금지 시작 시간", example = "22:00:00") LocalTime quietStart,
        @Schema(description = "방해 금지 종료 시간", example = "07:00:00") LocalTime quietEnd,
        
        @Schema(description = "랭킹 공개 설정", example = "true") Boolean rankingSetting,
        @Schema(description = "이름 공개 설정", example = "true") Boolean nameSetting,
        @Schema(description = "장소 공개 설정", example = "true") Boolean placeSetting,
        @Schema(description = "친구 공개 설정", example = "true") Boolean friendSetting
) {}
