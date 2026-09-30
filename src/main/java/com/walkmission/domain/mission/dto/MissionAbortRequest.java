package com.walkmission.domain.mission.dto;

import com.walkmission.domain.mission.entity.AbortReason;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;

@Schema(description = "미션 포기 요청 DTO (본문 생략 가능)")
public record MissionAbortRequest(
        @Schema(description = "포기 이유 (FARTHER_THAN_EXPECTED 생각보다 멀었어요, TIRED 피곤해졌어요, SOMETHING_CAME_UP 갑자기 일이 생겼어요, ROUTE_INCONVENIENT 길이 불편했어요)", example = "TIRED")
        AbortReason reason,

        @Schema(description = "포기 시점까지 이동한 거리(미터). 오늘 기록에 저장", example = "700")
        @Min(0) Integer movedDistanceMeters
) {}
