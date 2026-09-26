package com.walkmission.domain.mission.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "미션 추천 응답 DTO")
public record MissionRecommendResponse(
        @Schema(description = "미션 ID", example = "1001") Long missionId,
        @Schema(description = "미션 상태", example = "READY") String status,
        @Schema(description = "추천 목적지 정보") PlaceInfo place,
        @Schema(description = "목적지까지의 거리(미터)", example = "450") Integer distanceMeters,
        @Schema(description = "예상 소요 시간(분)", example = "10") Integer estDurationMinutes,
        @Schema(description = "예상 보상 포인트", example = "50") Integer estRewardPoint
) {
    public record PlaceInfo(
            @Schema(description = "장소 ID", example = "2001") Long placeId,
            @Schema(description = "장소 이름", example = "역삼동 근린공원") String name,
            @Schema(description = "장소 카테고리", example = "공원") String category,
            @Schema(description = "도로명 주소", example = "서울특별시 강남구 역삼로 123") String roadAddress,
            @Schema(description = "장소 위도", example = "37.499000") BigDecimal latitude,
            @Schema(description = "장소 경도", example = "127.028000") BigDecimal longitude
    ) {}
}
