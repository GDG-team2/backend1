package com.walkmission.domain.reward.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "전체 배지 도감 및 보유 현황 응답 DTO")
public record BadgeListResponse(
        @Schema(description = "배지 현황 요약") BadgeSummary summary,
        @Schema(description = "전체 배지 목록") List<BadgeDetail> badges
) {
    public record BadgeSummary(
            @Schema(description = "전체 배지 개수", example = "20") Integer totalCount,
            @Schema(description = "획득한 배지 개수", example = "5") Integer acquiredCount
    ) {}

    public record BadgeDetail(
            @Schema(description = "배지 ID", example = "1") Long badgeId,
            @Schema(description = "배지 이름", example = "첫 산책 마스터") String badgeName,
            @Schema(description = "배지 설명", example = "첫 산책을 무사히 완료했습니다.") String description,
            @Schema(description = "배지 아이콘 URL", example = "https://example.com/badges/1.png") String iconUrl,
            @Schema(description = "획득 여부", example = "true") Boolean isAcquired,
            @Schema(description = "대표 배지 설정 여부", example = "true") Boolean isRepresentative,
            @Schema(description = "획득 일시 (미획득시 null)") LocalDateTime acquiredAt,
            @Schema(description = "진행도: 현재 값 (목표를 넘으면 목표 값으로 표시)", example = "3") Integer progressCurrent,
            @Schema(description = "진행도: 목표 값", example = "5") Integer progressTarget
    ) {}
}
