package com.walkmission.domain.mission.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "미션 완료 및 정산 응답 DTO")
public record MissionCompleteResponse(
        @Schema(description = "미션 ID", example = "1001") Long missionId,
        @Schema(description = "미션 상태", example = "COMPLETED") String status,
        @Schema(description = "완료 일시") LocalDateTime completedAt,
        @Schema(description = "보상 포인트 정보") RewardInfo reward,
        @Schema(description = "랭킹 스코어 정보") RankingInfo ranking,
        @Schema(description = "스트릭 정보") StreakInfo streak,
        @Schema(description = "새로 획득한 배지 목록") List<BadgeInfo> newBadges
) {
    public record RewardInfo(
            @Schema(description = "이번 미션으로 획득한 포인트", example = "50") Integer earnedPoint,
            @Schema(description = "누적 보유 포인트", example = "1550") Integer currentTotalPoint
    ) {}

    public record RankingInfo(
            @Schema(description = "랭킹 참여 여부", example = "true") Boolean isParticipant,
            @Schema(description = "이번 미션으로 획득한 랭킹 점수", example = "100") Integer earnedScore,
            @Schema(description = "이번 주 누적 랭킹 점수", example = "500") Integer currentWeeklyScore
    ) {}

    public record StreakInfo(
            @Schema(description = "현재 스트릭", example = "6") Integer streakNow,
            @Schema(description = "스트릭 유지 여부", example = "true") Boolean isStreakMaintained
    ) {}

    public record BadgeInfo(
            @Schema(description = "획득한 배지 ID", example = "2") Long badgeId,
            @Schema(description = "배지 이름", example = "공원 탐험가") String badgeName,
            @Schema(description = "배지 설명", example = "공원 카테고리의 장소를 3번 방문했습니다.") String description,
            @Schema(description = "배지 아이콘 URL", example = "https://example.com/badges/2.png") String iconUrl
    ) {}
}
