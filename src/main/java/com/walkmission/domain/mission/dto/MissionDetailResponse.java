package com.walkmission.domain.mission.dto;

import com.walkmission.domain.mission.entity.AbortReason;
import com.walkmission.domain.mission.entity.Mood;
import com.walkmission.domain.mission.entity.MoveType;
import com.walkmission.domain.mission.entity.PlaceCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "미션 기록 상세 응답 DTO (D06)")
public record MissionDetailResponse(
        @Schema(description = "미션 ID", example = "1001") Long missionId,
        @Schema(description = "미션 상태", example = "COMPLETED") String status,
        @Schema(description = "미션 제목", example = "화랑유원지 한 바퀴") String missionTitle,
        @Schema(description = "추천 이유") String reason,
        @Schema(description = "장소") MissionRecommendResponse.PlaceInfo place,
        @Schema(description = "범주", example = "WALK") PlaceCategory placeCategory,
        @Schema(description = "이동수단", example = "WALK") MoveType moveType,
        @Schema(description = "그때 처음 가본 곳이었는지", example = "true") Boolean isNewPlace,
        @Schema(description = "예상 왕복 이동 거리(미터)", example = "2300") Integer routeDistanceMeters,
        @Schema(description = "소요 시간(분)", example = "54") Integer durationMinutes,
        @Schema(description = "예상 비용(원)", example = "0") Integer estCost,
        @Schema(description = "걸음 수", example = "3200") Integer stepCount,
        @Schema(description = "기분 변화") MoodChange mood,
        @Schema(description = "미션 타임라인 (시간 순)") List<TimelineEvent> timeline,
        @Schema(description = "포기한 미션이면 이유와 이동 거리 (아니면 null)") AbortInfo abort
) {
    public record MoodChange(
            @Schema(description = "추천받을 때 고른 기분", example = "GLOOMY") Mood beforeMood,
            @Schema(description = "산책 전 점수 (1~5)", example = "2") Integer beforeScore,
            @Schema(description = "산책 후 점수 (1~5)", example = "4") Integer afterScore,
            @Schema(description = "변화량 (후 - 전)", example = "2") Integer change
    ) {}

    public record TimelineEvent(
            @Schema(description = "종류 (SCHEDULED 약속, STARTED 출발, ARRIVED 도착, COMPLETED 완료, ABORTED 중단)", example = "STARTED") String type,
            @Schema(description = "일시") LocalDateTime at,
            @Schema(description = "설명", example = "약속보다 3분 빠르게 시작") String note
    ) {}

    public record AbortInfo(
            @Schema(description = "포기 이유", example = "TIRED") AbortReason reason,
            @Schema(description = "그때까지 이동한 거리(미터)", example = "700") Integer movedDistanceMeters
    ) {}
}
