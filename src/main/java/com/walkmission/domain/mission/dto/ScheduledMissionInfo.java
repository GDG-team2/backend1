package com.walkmission.domain.mission.dto;

import com.walkmission.domain.mission.entity.MoveType;
import com.walkmission.domain.mission.entity.PlaceCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "출발을 약속한 미션")
public record ScheduledMissionInfo(
        @Schema(description = "미션 ID", example = "1001") Long missionId,
        @Schema(description = "미션 제목", example = "화랑유원지 한 바퀴") String missionTitle,
        @Schema(description = "출발 약속 시각") LocalDateTime scheduledAt,
        @Schema(description = "출발까지 남은 분 (지났으면 0)", example = "42") Integer minutesUntilDeparture,
        @Schema(description = "약속 시각이 지났는지 (지나도 패널티 없이 출발 가능)", example = "false") Boolean isOverdue,
        @Schema(description = "장소 이름", example = "화랑유원지") String placeName,
        @Schema(description = "범주", example = "WALK") PlaceCategory placeCategory,
        @Schema(description = "이동수단", example = "WALK") MoveType moveType,
        @Schema(description = "예상 편도 소요 시간(분)", example = "17") Integer oneWayMinutes,
        @Schema(description = "전체 예상 시간(분)", example = "52") Integer totalMinutes,
        @Schema(description = "예상 비용(원)", example = "0") Integer estCost
) {}
