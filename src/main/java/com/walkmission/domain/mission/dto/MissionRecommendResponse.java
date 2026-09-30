package com.walkmission.domain.mission.dto;

import com.walkmission.domain.mission.entity.MoveType;
import com.walkmission.domain.mission.entity.PlaceCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "미션 추천 응답 DTO")
public record MissionRecommendResponse(
        @Schema(description = "미션 ID", example = "1001") Long missionId,
        @Schema(description = "미션 상태", example = "READY") String status,
        @Schema(description = "추천 목적지 정보") PlaceInfo place,
        @Schema(description = "목적지까지의 직선거리(미터)", example = "650") Integer distanceMeters,
        @Schema(description = "예상 편도 소요 시간(분). 이동수단과 실제 경로 우회를 반영", example = "13") Integer estDurationMinutes,
        @Schema(description = "예상 보상 포인트", example = "50") Integer estRewardPoint,
        @Schema(description = "추천에 사용된 범주", example = "WALK") PlaceCategory placeCategory,
        @Schema(description = "추천에 사용된 이동수단", example = "WALK") MoveType moveType
) {
    public record PlaceInfo(
            @Schema(description = "장소 ID", example = "2001") Long placeId,
            @Schema(description = "카카오 장소 ID", example = "18577297") String kakaoPlaceId,
            @Schema(description = "장소 이름", example = "역삼동 근린공원") String name,
            @Schema(description = "장소 세부 분류 (카카오 기준)", example = "도시근린공원") String category,
            @Schema(description = "도로명 주소", example = "서울특별시 강남구 역삼로 123") String roadAddress,
            @Schema(description = "장소 위도", example = "37.499000") BigDecimal latitude,
            @Schema(description = "장소 경도", example = "127.028000") BigDecimal longitude,
            @Schema(description = "카카오맵 장소 상세 URL", example = "http://place.map.kakao.com/18577297") String placeUrl
    ) {}
}
