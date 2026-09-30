package com.walkmission.domain.mission.dto;

import com.walkmission.domain.mission.entity.Budget;
import com.walkmission.domain.mission.entity.MoveType;
import com.walkmission.domain.mission.entity.PlaceCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "미션 추천 응답 DTO")
public record MissionRecommendResponse(
        @Schema(description = "미션 ID", example = "1001") Long missionId,
        @Schema(description = "미션 상태", example = "READY") String status,
        @Schema(description = "미션 제목", example = "화랑유원지 한 바퀴") String missionTitle,
        @Schema(description = "추천 이유 (\"왜 이 미션인가요?\")", example = "최근 카페 방문이 3회 연속이었어요. 공원 외출 뒤 만족도가 더 높았어요.") String reason,
        @Schema(description = "추천 목적지 정보") PlaceInfo place,
        @Schema(description = "목적지까지의 직선거리(미터)", example = "780") Integer distanceMeters,
        @Schema(description = "실제 길 기준 예상 왕복 이동 거리(미터)", example = "2028") Integer routeDistanceMeters,
        @Schema(description = "예상 편도 소요 시간(분)", example = "17") Integer oneWayMinutes,
        @Schema(description = "전체 예상 시간(분) = 왕복 이동 + 장소에서 보내는 시간", example = "49") Integer totalMinutes,
        @Schema(description = "예상 비용(원, 범주 평균)", example = "0") Integer estCost,
        @Schema(description = "처음 가보는 장소인지", example = "true") Boolean isNewPlace,
        @Schema(description = "예상 보상 포인트", example = "50") Integer estRewardPoint,
        @Schema(description = "추천에 사용된 범주", example = "WALK") PlaceCategory placeCategory,
        @Schema(description = "추천에 사용된 이동수단", example = "WALK") MoveType moveType,
        @Schema(description = "추천에 사용된 예산", example = "ANY") Budget budget
) {
    public record PlaceInfo(
            @Schema(description = "장소 ID", example = "2001") Long placeId,
            @Schema(description = "카카오 장소 ID", example = "18577297") String kakaoPlaceId,
            @Schema(description = "장소 이름", example = "화랑유원지") String name,
            @Schema(description = "장소 세부 분류 (카카오 기준)", example = "도시근린공원") String category,
            @Schema(description = "도로명 주소", example = "경기 안산시 단원구 동산로 268") String roadAddress,
            @Schema(description = "장소 위도", example = "37.312000") BigDecimal latitude,
            @Schema(description = "장소 경도", example = "126.822000") BigDecimal longitude,
            @Schema(description = "카카오맵 장소 상세 URL", example = "http://place.map.kakao.com/18577297") String placeUrl
    ) {}
}
