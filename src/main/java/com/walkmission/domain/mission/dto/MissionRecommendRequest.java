package com.walkmission.domain.mission.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "미션 추천 요청 DTO")
public record MissionRecommendRequest(
        @Schema(description = "사용자 현재 위도", example = "37.498095")
        @NotNull BigDecimal latitude,
        
        @Schema(description = "사용자 현재 경도", example = "127.027610")
        @NotNull BigDecimal longitude
) {}
