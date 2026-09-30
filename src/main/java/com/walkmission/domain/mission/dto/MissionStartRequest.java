package com.walkmission.domain.mission.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Schema(description = "미션 출발 요청 DTO (본문 생략 가능)")
public record MissionStartRequest(
        @Schema(description = "사전 설문 별점 (1~5, 선택)", example = "3")
        @Min(1) @Max(5) Integer beforeSurveyScore
) {}
