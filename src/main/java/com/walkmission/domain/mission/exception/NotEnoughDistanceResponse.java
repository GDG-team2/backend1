package com.walkmission.domain.mission.exception;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "도착 인증 실패 응답 DTO")
public record NotEnoughDistanceResponse(
        @Schema(description = "에러 코드", example = "NOT_ENOUGH_DISTANCE") String code,
        @Schema(description = "에러 메시지", example = "목적지 반경 50m 이내에 도착하지 않았습니다.") String message,
        @Schema(description = "목적지까지 남은 거리(미터)", example = "120") Integer currentDistanceMeters
) {}
