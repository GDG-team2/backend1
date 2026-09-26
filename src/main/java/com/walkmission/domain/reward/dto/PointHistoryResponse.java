package com.walkmission.domain.reward.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "포인트 거래 내역 페이징 응답 DTO")
public record PointHistoryResponse(
        @Schema(description = "현재 보유 포인트", example = "1500") Integer currentPoint,
        @Schema(description = "포인트 내역 목록") List<PointHistoryEntry> history,
        @Schema(description = "페이징 정보") PaginationInfo pagination
) {
    public record PointHistoryEntry(
            @Schema(description = "내역 ID", example = "1") Long historyId,
            @Schema(description = "거래 타입 (EARN, USE)", example = "EARN") String type,
            @Schema(description = "변동 금액", example = "50") Integer amount,
            @Schema(description = "거래 설명", example = "미션 완주 보상") String description,
            @Schema(description = "거래 일시") LocalDateTime createdAt
    ) {}

    public record PaginationInfo(
            @Schema(description = "현재 페이지 번호", example = "0") Integer currentPage,
            @Schema(description = "페이지 크기", example = "20") Integer pageSize,
            @Schema(description = "전체 페이지 수", example = "5") Integer totalPages,
            @Schema(description = "전체 데이터 수", example = "95") Long totalElements,
            @Schema(description = "다음 페이지 존재 여부", example = "true") Boolean hasNext
    ) {}
}
