package com.walkmission.domain.mission.dto;

import com.walkmission.domain.mission.entity.Budget;
import com.walkmission.domain.mission.entity.Mood;
import com.walkmission.domain.mission.entity.MoveType;
import com.walkmission.domain.mission.entity.PlaceCategory;
import com.walkmission.domain.mission.entity.RejectReason;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "미션 추천 요청 DTO. 위치 외에는 모두 선택이며, 생략하면 선호 설정 값을 쓴다")
public record MissionRecommendRequest(
        @Schema(description = "사용자 현재 위도", example = "37.498095")
        @NotNull BigDecimal latitude,
        
        @Schema(description = "사용자 현재 경도", example = "127.027610")
        @NotNull BigDecimal longitude,

        @Schema(description = "지금 기분 (TIRED 지침, BORED 심심함, GLOOMY 꿀꿀함, ENERGETIC 활기참). 거리와 범주 우선순위에 반영", example = "GLOOMY")
        Mood mood,

        @Schema(description = "이번 추천에만 적용할 범주", example = "CAFE")
        PlaceCategory category,

        @Schema(description = "이번 추천에만 적용할 이동수단", example = "WALK")
        MoveType moveType,

        @Schema(description = "이번 추천에만 적용할 예산 (FREE 0원, UNDER_10K 1만원, UNDER_30K 3만원, ANY 상관없음)", example = "FREE")
        Budget budget,

        @Schema(description = "다시 추천받을 때, 이전 추천을 거절한 이유 (TOO_FAR, DISLIKE_ACTIVITY, ALREADY_VISITED, NO_SPENDING). 이번 추천에 바로 반영", example = "TOO_FAR")
        RejectReason rejectReason
) {}
