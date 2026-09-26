package com.walkmission.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "회원가입 응답 DTO")
public record SignupResponse(
        @Schema(description = "사용자 고유 UUID", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID userUuid,
        
        @Schema(description = "닉네임", example = "walking_master")
        String nickname,
        
        @Schema(description = "응답 메시지", example = "회원가입이 완료되었습니다.")
        String message
) {}
