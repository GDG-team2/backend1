package com.walkmission.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Schema(description = "회원가입 요청 DTO")
public record SignupRequest(
        @Schema(description = "이메일", example = "user@example.com")
        @NotBlank @Email String email,
        
        @Schema(description = "비밀번호", example = "password123!")
        @NotBlank String password,
        
        @Schema(description = "닉네임", example = "walking_master")
        @NotBlank String nickname,
        
        @Schema(description = "생년월일", example = "1990-01-01")
        @NotNull LocalDate birth,
        
        @Schema(description = "동네 코드 (법정동/행정동)", example = "1168010100")
        @NotBlank String regionCode
) {}
