package com.walkmission.domain.mission.dto;

import com.walkmission.domain.mission.entity.MoveType;
import com.walkmission.domain.mission.entity.PlaceCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "미션 추천 요청 DTO")
public record MissionRecommendRequest(
        @Schema(description = "사용자 현재 위도", example = "37.498095")
        @NotNull BigDecimal latitude,
        
        @Schema(description = "사용자 현재 경도", example = "127.027610")
        @NotNull BigDecimal longitude,

        @Schema(description = "이번 추천에만 적용할 범주 (선택). 생략하면 선호 설정의 범주 사용", example = "CAFE")
        PlaceCategory category,

        @Schema(description = "이번 추천에만 적용할 이동수단 (선택). 생략하면 선호 설정의 이동수단 사용", example = "WALK")
        MoveType moveType
) {}
