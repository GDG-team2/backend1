package com.walkmission.domain.mission.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "미션 완료 및 설문 제출 요청 DTO")
public record MissionCompleteRequest(
        @Schema(description = "사후 설문 별점 (1~5)", example = "5")
        @NotNull @Min(1) @Max(5) Integer afterSurveyScore
) {}
