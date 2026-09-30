package com.walkmission.domain.mission.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "내 지도 응답 DTO (06): 기간 내 다녀온 장소와 통계")
public record MissionMapResponse(
        @Schema(description = "조회 기간") PeriodInfo period,
        @Schema(description = "통계") Stats stats,
        @Schema(description = "다녀온 장소 (최근 방문 순). 지도 핀용") List<VisitedPlace> places,
        @Schema(description = "최근 기록 (최대 5건)") List<MissionRecordItem> recent
) {
    public record Stats(
            @Schema(description = "외출 횟수", example = "13") Integer outingCount,
            @Schema(description = "새로 가본 장소 수", example = "7") Integer newPlaceCount,
            @Schema(description = "예상 이동 거리 합계(미터)", example = "28400") Integer totalRouteDistanceMeters
    ) {}

    public record VisitedPlace(
            @Schema(description = "장소 ID", example = "2001") Long placeId,
            @Schema(description = "장소 이름", example = "화랑유원지") String name,
            @Schema(description = "장소 세부 분류 (카카오 기준)", example = "도시근린공원") String category,
            @Schema(description = "위도", example = "37.312000") BigDecimal latitude,
            @Schema(description = "경도", example = "126.822000") BigDecimal longitude,
            @Schema(description = "기간 내 방문 횟수", example = "2") Integer visitCount,
            @Schema(description = "마지막 방문 일시") LocalDateTime lastVisitedAt
    ) {}
}
