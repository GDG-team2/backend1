package com.walkmission.domain.mission.dto;

import com.walkmission.domain.mission.entity.HistoryPeriod;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(description = "조회 기간 (ALL이면 시작·종료일 null)")
public record PeriodInfo(
        @Schema(description = "기간 종류", example = "MONTH") HistoryPeriod type,
        @Schema(description = "시작일", example = "2026-08-01") LocalDate startDate,
        @Schema(description = "종료일(포함)", example = "2026-08-31") LocalDate endDate
) {}
