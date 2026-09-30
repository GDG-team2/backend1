package com.walkmission.domain.mission.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "활동 기록 응답 DTO (M02)")
public record MissionHistoryResponse(
        @Schema(description = "조회 기간") PeriodInfo period,
        @Schema(description = "기간·필터에 해당하는 전체 기록 요약") Summary summary,
        @Schema(description = "기록 목록 (최신순)") List<MissionRecordItem> records,
        @Schema(description = "페이징 정보") Pagination pagination
) {
    public record Summary(
            @Schema(description = "완료 횟수", example = "13") Integer completedCount,
            @Schema(description = "밖에서 보낸 시간 합계(분)", example = "702") Integer totalOutdoorMinutes,
            @Schema(description = "평균 만족도 (기록 없으면 null)", example = "4.2") Double averageSatisfaction,
            @Schema(description = "예상 이동 거리 합계(미터)", example = "28400") Integer totalRouteDistanceMeters,
            @Schema(description = "새로 가본 장소 수", example = "7") Integer newPlaceCount
    ) {}

    public record Pagination(
            @Schema(description = "현재 페이지 번호", example = "0") Integer currentPage,
            @Schema(description = "페이지 크기", example = "20") Integer pageSize,
            @Schema(description = "전체 페이지 수", example = "1") Integer totalPages,
            @Schema(description = "전체 데이터 수", example = "13") Long totalElements,
            @Schema(description = "다음 페이지 존재 여부", example = "false") Boolean hasNext
    ) {}
}
