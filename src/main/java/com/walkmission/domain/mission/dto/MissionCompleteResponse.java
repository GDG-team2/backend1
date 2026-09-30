package com.walkmission.domain.mission.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "미션 완료 및 정산 응답 DTO")
public record MissionCompleteResponse(
        @Schema(description = "미션 ID", example = "1001") Long missionId,
        @Schema(description = "미션 상태", example = "COMPLETED") String status,
        @Schema(description = "완료 일시") LocalDateTime completedAt,
        @Schema(description = "기록된 걸음 수", example = "1850") Integer stepCount,
        @Schema(description = "처음 가본 장소인지 (\"새로운 장소 +1\")", example = "true") Boolean isNewPlace,
        @Schema(description = "출발부터 완료까지 걸린 시간(분)", example = "54") Integer durationMinutes,
        @Schema(description = "보상 포인트 정보") RewardInfo reward,
        @Schema(description = "랭킹 점수 정보") RankingInfo ranking,
        @Schema(description = "주간 리듬 정보") RhythmInfo rhythm,
        @Schema(description = "새로 획득한 배지 목록") List<BadgeInfo> newBadges
) {
    public record RewardInfo(
            @Schema(description = "이번 미션으로 획득한 포인트", example = "50") Integer earnedPoint,
            @Schema(description = "누적 보유 포인트", example = "1550") Integer currentTotalPoint
    ) {}

    public record RankingInfo(
            @Schema(description = "랭킹 참여 여부 (랭킹 공개 설정)", example = "true") Boolean isParticipant,
            @Schema(description = "이번 미션으로 얻은 점수 (보너스 포함). 이번 주 반영 한도를 넘었으면 0", example = "120") Integer earnedScore,
            @Schema(description = "earnedScore 중 다양성 보너스 (그 주에 처음 해본 범주)", example = "20") Integer bonusScore,
            @Schema(description = "이번 주 누적 랭킹 점수", example = "230") Integer currentWeeklyScore,
            @Schema(description = "이번 주 점수에 반영된 미션 수", example = "2") Integer scoredMissionCount,
            @Schema(description = "주간 점수 반영 최대 횟수", example = "3") Integer maxScoredMissions
    ) {}

    public record RhythmInfo(
            @Schema(description = "주간 목표 (주 N회)", example = "3") Integer weeklyGoal,
            @Schema(description = "이번 주 완료 횟수", example = "3") Integer thisWeekCount,
            @Schema(description = "이번 미션으로 이번 주 목표를 막 채웠는지", example = "true") Boolean goalJustAchieved,
            @Schema(description = "목표를 연속으로 채운 주 수", example = "4") Integer currentWeeks
    ) {}

    public record BadgeInfo(
            @Schema(description = "획득한 배지 ID", example = "2") Long badgeId,
            @Schema(description = "배지 이름", example = "새 길 발견") String badgeName,
            @Schema(description = "배지 설명", example = "서로 다른 장소 5곳을 방문했어요.") String description,
            @Schema(description = "배지 아이콘 URL", example = "https://example.com/badges/2.png") String iconUrl
    ) {}
}
