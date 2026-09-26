package com.walkmission.domain.mission.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "미션 도착 인증 응답 DTO")
public record MissionArriveResponse(
        @Schema(description = "미션 ID", example = "1001") Long missionId,
        @Schema(description = "미션 상태", example = "ARRIVED") String status,
        @Schema(description = "도착 인증 일시") LocalDateTime arrivedAt,
        @Schema(description = "응답 메시지", example = "목적지에 도착했습니다! 주변을 둘러보세요.") String message
) {}
