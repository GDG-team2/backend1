package com.walkmission.domain.mission.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "진행 중인 미션 조회 응답 DTO")
public record CurrentMissionResponse(
        @Schema(description = "진행 중인 미션 존재 여부", example = "true") Boolean hasActiveMission,
        @Schema(description = "진행 중인 미션 정보 (없을 경우 null)") ActiveMissionInfo mission
) {
    public record ActiveMissionInfo(
            @Schema(description = "미션 ID", example = "1001") Long missionId,
            @Schema(description = "미션 상태", example = "IN_PROGRESS") String status,
            @Schema(description = "출발 일시 (READY 상태일 땐 null)") LocalDateTime startedAt,
            @Schema(description = "목적지 장소 정보") PlaceInfo place,
            @Schema(description = "예상 보상 포인트", example = "50") Integer estRewardPoint
    ) {}

    public record PlaceInfo(
            @Schema(description = "장소 ID", example = "2001") Long placeId,
            @Schema(description = "카카오 장소 ID", example = "18577297") String kakaoPlaceId,
            @Schema(description = "장소 이름", example = "역삼동 근린공원") String name,
            @Schema(description = "장소 카테고리", example = "공원") String category,
            @Schema(description = "도로명 주소", example = "서울특별시 강남구 역삼로 123") String roadAddress,
            @Schema(description = "장소 위도", example = "37.499000") BigDecimal latitude,
            @Schema(description = "장소 경도", example = "127.028000") BigDecimal longitude
    ) {}
}
