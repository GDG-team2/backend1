package com.walkmission.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Schema(description = "알림 및 공개 설정 변경 응답 DTO")
public record UserSettingsUpdateResponse(
        @Schema(description = "사용자 고유 UUID", example = "123e4567-e89b-12d3-a456-426614174000") UUID userUuid,
        @Schema(description = "전체 알림") Boolean allAlarm,
        @Schema(description = "시작 알림") Boolean startAlarm,
        @Schema(description = "미션 알림") Boolean missionAlarm,
        @Schema(description = "인사이트 알림") Boolean insightAlarm,
        @Schema(description = "보상 알림") Boolean rewardAlarm,
        @Schema(description = "방해 금지 시작 시간") LocalTime quietStart,
        @Schema(description = "방해 금지 종료 시간") LocalTime quietEnd,
        @Schema(description = "랭킹 공개 설정") Boolean rankingSetting,
        @Schema(description = "이름 공개 설정") Boolean nameSetting,
        @Schema(description = "장소 공개 설정") Boolean placeSetting,
        @Schema(description = "친구 공개 설정") Boolean friendSetting,
        @Schema(description = "수정 일시") LocalDateTime updatedAt,
        @Schema(description = "응답 메시지", example = "설정이 성공적으로 변경되었습니다.") String message
) {}
