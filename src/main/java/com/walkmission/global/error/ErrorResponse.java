package com.walkmission.global.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;

@Schema(description = "공통 에러 응답")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        @Schema(description = "HTTP 상태 코드", example = "400") int status,
        @Schema(description = "에러 코드 (프론트 분기용)", example = "NOT_ENOUGH_DISTANCE") String code,
        @Schema(description = "사용자에게 보여줄 수 있는 메시지", example = "목적지 반경 50m 이내에 도착하지 않았습니다.") String message,
        @Schema(description = "추가 정보 (없으면 생략). 예: 남은 거리, 필드별 검증 오류", example = "{\"currentDistanceMeters\": 120}")
        Map<String, Object> details
) {
    public static ErrorResponse of(ErrorCode code) {
        return of(code, null);
    }

    public static ErrorResponse of(ErrorCode code, Map<String, Object> details) {
        return new ErrorResponse(code.getStatus().value(), code.name(), code.getMessage(), details);
    }
}
