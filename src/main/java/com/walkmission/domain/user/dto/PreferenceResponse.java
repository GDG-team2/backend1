package com.walkmission.domain.user.dto;

import com.walkmission.domain.mission.entity.Budget;
import com.walkmission.domain.mission.entity.MoveType;
import com.walkmission.domain.mission.entity.PlaceCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "미션 선호 설정 응답 DTO")
public record PreferenceResponse(
        @Schema(description = "전체 외출 시간(분, 장소에서 보내는 시간 포함)", example = "60") Integer walkTime,
        @Schema(description = "이동수단", example = "WALK") MoveType moveType,
        @Schema(description = "추천에 사용되는 범주 (선택하지 않았으면 전체)", example = "[\"WALK\", \"CAFE\"]") List<PlaceCategory> categories,
        @Schema(description = "주간 리듬 목표 (주 N회)", example = "3") Integer weeklyGoal,
        @Schema(description = "부담 없는 예산", example = "UNDER_10K") Budget budget
) {}
