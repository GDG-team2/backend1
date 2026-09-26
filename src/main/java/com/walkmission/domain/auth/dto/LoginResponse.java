package com.walkmission.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "로그인 응답 DTO")
public record LoginResponse(
        @Schema(description = "액세스 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String accessToken,
        
        @Schema(description = "리프레시 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String refreshToken,
        
        @Schema(description = "사용자 고유 UUID", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID userUuid,
        
        @Schema(description = "닉네임", example = "walking_master")
        String nickname
) {}
