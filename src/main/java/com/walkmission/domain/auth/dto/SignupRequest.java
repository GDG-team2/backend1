package com.walkmission.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "회원가입 요청 DTO")
public record SignupRequest(
        @Schema(description = "이메일", example = "user@example.com")
        @NotBlank @Email String email,
        
        @Schema(description = "비밀번호 (영문·숫자 포함 8자 이상)", example = "password123!")
        @NotBlank String password,
        
        @Schema(description = "닉네임", example = "선우")
        @NotBlank String nickname,
        
        @Schema(description = "출생 연도 (선택)", example = "2006")
        @Min(1900) Integer birthYear,
        
        @Schema(description = "동네 코드 (법정동/행정동)", example = "4127310500")
        @NotBlank String regionCode,

        @Schema(description = "약관 동의. service·privacy·location은 필수(true), marketing은 선택")
        @NotNull @Valid Agreements agreements
) {
    public record Agreements(
            @Schema(description = "서비스 이용약관 (필수)", example = "true") Boolean service,
            @Schema(description = "개인정보 처리방침 (필수)", example = "true") Boolean privacy,
            @Schema(description = "위치기반서비스 이용약관 (필수)", example = "true") Boolean location,
            @Schema(description = "혜택·이벤트 알림 수신 (선택)", example = "false") Boolean marketing
    ) {}
}
