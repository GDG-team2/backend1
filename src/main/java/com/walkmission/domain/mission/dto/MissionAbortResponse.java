package com.walkmission.domain.mission.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "미션 중단/포기 응답 DTO")
public record MissionAbortResponse(
        @Schema(description = "미션 ID", example = "1001") Long missionId,
        @Schema(description = "미션 상태", example = "ABORTED") String status,
        @Schema(description = "중단 일시") LocalDateTime abortedAt,
        @Schema(description = "저장된 이동 거리(미터). 보내지 않았으면 null", example = "700") Integer movedDistanceMeters,
        @Schema(description = "다음 추천에 반영되는 내용 (없으면 null)", example = "오늘은 더 가까운 곳부터 제안할게요.") String nextRecommendationNote,
        @Schema(description = "응답 메시지", example = "멈춰도 기록은 남아요.") String message
) {}
