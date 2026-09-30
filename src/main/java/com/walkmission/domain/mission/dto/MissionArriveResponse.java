package com.walkmission.domain.mission.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "미션 도착 인증 응답 DTO. 반경 안에서 체류 시간을 채우는 중이면 status는 IN_PROGRESS로 유지된다")
public record MissionArriveResponse(
        @Schema(description = "미션 ID", example = "1001") Long missionId,
        @Schema(description = "미션 상태. 체류 확인 중이면 IN_PROGRESS, 인증되면 ARRIVED", example = "ARRIVED") String status,
        @Schema(description = "도착 인증 일시 (체류 확인 중이면 null)") LocalDateTime arrivedAt,
        @Schema(description = "도착 인정까지 더 머물러야 하는 초. 0이면 인증 완료. 이 시간이 지난 뒤 같은 요청을 다시 보내면 된다", example = "0") Integer remainingDwellSeconds,
        @Schema(description = "응답 메시지", example = "목적지에 도착했어요! 주변을 둘러보세요.") String message
) {}
