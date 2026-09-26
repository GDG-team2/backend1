package com.walkmission.domain.mission.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "미션 중단/포기 응답 DTO")
public record MissionAbortResponse(
        @Schema(description = "미션 ID", example = "1001") Long missionId,
        @Schema(description = "미션 상태", example = "ABORTED") String status,
        @Schema(description = "중단 일시") LocalDateTime abortedAt,
        @Schema(description = "응답 메시지", example = "산책 미션을 포기했습니다.") String message
) {}
