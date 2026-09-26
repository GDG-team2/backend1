package com.walkmission.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "마이페이지 프로필/자산 조회 응답 DTO")
public record UserProfileResponse(
        @Schema(description = "사용자 고유 UUID", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID userUuid,
        
        @Schema(description = "이메일", example = "user@example.com")
        String email,
        
        @Schema(description = "닉네임", example = "walking_master")
        String nickname,
        
        @Schema(description = "동네 정보")
        RegionInfo region,
        
        @Schema(description = "자산 정보")
        AssetInfo asset,
        
        @Schema(description = "스트릭 정보")
        StreakInfo streak,
        
        @Schema(description = "대표 배지 정보")
        BadgeInfo representativeBadge,
        
        @Schema(description = "통계 정보")
        StatsInfo stats
) {
    public record RegionInfo(
            @Schema(description = "동네 코드", example = "1168010100") String regionCode,
            @Schema(description = "동네 이름", example = "서울특별시 강남구 역삼동") String regionName
    ) {}

    public record AssetInfo(
            @Schema(description = "현재 포인트", example = "1500") Integer currentPoint
    ) {}

    public record StreakInfo(
            @Schema(description = "현재 연속 스트릭", example = "5") Integer streakNow,
            @Schema(description = "최고 연속 스트릭 기록", example = "14") Integer streakRecord
    ) {}

    public record BadgeInfo(
            @Schema(description = "배지 ID", example = "1") Long badgeId,
            @Schema(description = "배지 이름", example = "첫 산책 마스터") String badgeName,
            @Schema(description = "배지 아이콘 URL", example = "https://example.com/badges/1.png") String iconUrl
    ) {}

    public record StatsInfo(
            @Schema(description = "총 완료 미션 수", example = "42") Integer totalCompletedMissions
    ) {}
}
