package com.walkmission.domain.user.dto;

import com.walkmission.domain.mission.entity.Budget;
import com.walkmission.domain.mission.entity.MoveType;
import com.walkmission.domain.mission.entity.PlaceCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.Set;

@Schema(description = "미션 선호 설정 변경 요청 DTO (모든 필드 선택, 보낸 필드만 변경)")
public record PreferenceUpdateRequest(
        @Schema(description = "전체 외출 시간(분, 장소에서 보내는 시간 포함, 10~120)", example = "60")
        @Min(10) @Max(120) Integer walkTime,

        @Schema(description = "이동수단 (WALK 도보, PUBLIC_TRANSIT 대중교통, BIKE 자전거)", example = "WALK")
        MoveType moveType,

        @Schema(description = "선호 범주 (WALK 산책, CAFE 카페, SIGHTSEEING 구경, EXHIBITION 전시, FOOD 먹기). 빈 배열이면 전체 범주", example = "[\"WALK\", \"CAFE\"]")
        Set<PlaceCategory> categories,

        @Schema(description = "주간 리듬 목표 (주 N회, 1~7)", example = "3")
        @Min(1) @Max(7) Integer weeklyGoal,

        @Schema(description = "부담 없는 예산 (FREE 0원, UNDER_10K 1만원, UNDER_30K 3만원, ANY 상관없음)", example = "UNDER_10K")
        Budget budget
) {}
