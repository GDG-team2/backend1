package com.walkmission.domain.mission.dto;

import com.walkmission.domain.mission.entity.PlaceCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "완료한 미션 기록 한 건")
public record MissionRecordItem(
        @Schema(description = "미션 ID", example = "1001") Long missionId,
        @Schema(description = "미션 제목", example = "화랑유원지 한 바퀴") String missionTitle,
        @Schema(description = "장소 이름", example = "화랑유원지") String placeName,
        @Schema(description = "범주", example = "WALK") PlaceCategory placeCategory,
        @Schema(description = "완료 일시") LocalDateTime completedAt,
        @Schema(description = "출발부터 완료까지 걸린 시간(분)", example = "54") Integer durationMinutes,
        @Schema(description = "예상 왕복 이동 거리(미터, 실제 길 기준 추정)", example = "2300") Integer routeDistanceMeters,
        @Schema(description = "만족도 (사후 기분 1~5)", example = "4") Integer satisfaction,
        @Schema(description = "그때 처음 가본 곳이었는지", example = "true") Boolean isNewPlace,
        @Schema(description = "예상 비용(원)", example = "0") Integer estCost
) {}
