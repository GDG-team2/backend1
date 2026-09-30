package com.walkmission.domain.mission.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Schema(description = "출발 약속 요청 DTO")
public record MissionScheduleRequest(
        @Schema(description = "출발 약속 시각 (지금부터 14일 이내, 한국 시간)", example = "2026-09-30T17:30:00")
        @NotNull LocalDateTime departAt
) {}
