package com.walkmission.domain.ranking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Schema(description = "동네 주간 랭킹 리더보드 응답 DTO")
public record RegionRankingResponse(
        @Schema(description = "동네 정보") RegionInfo region,
        @Schema(description = "주간 기간 정보") WeekPeriodInfo weekPeriod,
        @Schema(description = "나의 랭킹 정보") RankingEntry myRanking,
        @Schema(description = "리더보드 목록") List<RankingEntry> leaderboard
) {
    public record RegionInfo(
            @Schema(description = "동네 코드", example = "1168010100") String regionCode,
            @Schema(description = "동네 이름", example = "서울특별시 강남구 역삼동") String regionName
    ) {}

    public record WeekPeriodInfo(
            @Schema(description = "주간 시작일", example = "2023-10-23") LocalDate startDate,
            @Schema(description = "주간 종료일", example = "2023-10-29") LocalDate endDate
    ) {}

    public record RankingEntry(
            @Schema(description = "랭킹 참여 여부 (본인 조회시에만 사용)", example = "true") Boolean isParticipating,
            @Schema(description = "사용자 고유 UUID", example = "123e4567-e89b-12d3-a456-426614174000") UUID userUuid,
            @Schema(description = "닉네임", example = "walking_master") String nickname,
            @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.png") String profileImageUrl,
            @Schema(description = "순위", example = "1") Integer rank,
            @Schema(description = "주간 점수", example = "1250") Integer score,
            @Schema(description = "이번 주 점수에 반영된 미션 수 (주 최대 3회)", example = "2") Integer scoredMissionCount,
            @Schema(description = "이번 주 다양성 보너스 합계", example = "20") Integer bonusScore
    ) {}
}
