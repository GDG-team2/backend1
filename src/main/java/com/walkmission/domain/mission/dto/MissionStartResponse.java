package com.walkmission.domain.mission.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "미션 출발 응답 DTO")
public record MissionStartResponse(
        @Schema(description = "미션 ID", example = "1001") Long missionId,
        @Schema(description = "미션 상태", example = "IN_PROGRESS") String status,
        @Schema(description = "출발 일시") LocalDateTime startedAt,
        @Schema(description = "응답 메시지", example = "산책 미션을 시작했습니다. 안전하게 이동하세요!") String message
) {}
