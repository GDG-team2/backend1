package com.walkmission.domain.mission.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "예정 미션 목록 응답 DTO (가까운 시각 순)")
public record ScheduledMissionsResponse(
        @Schema(description = "예정 미션 목록") List<ScheduledMissionInfo> missions
) {}
