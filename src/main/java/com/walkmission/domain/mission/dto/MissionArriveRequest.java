package com.walkmission.domain.mission.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "미션 도착 인증 요청 DTO")
public record MissionArriveRequest(
        @Schema(description = "도착 시점의 현재 위도", example = "37.499010")
        @NotNull BigDecimal latitude,
        
        @Schema(description = "도착 시점의 현재 경도", example = "127.028010")
        @NotNull BigDecimal longitude
) {}
