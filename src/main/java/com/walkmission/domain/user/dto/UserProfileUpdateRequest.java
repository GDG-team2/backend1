package com.walkmission.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

@Schema(description = "프로필 수정 요청 DTO (모든 필드 선택, 보낸 필드만 변경)")
public record UserProfileUpdateRequest(
        @Schema(description = "닉네임 (1~20자)", example = "선우")
        @Size(min = 1, max = 20) String nickname,

        @Schema(description = "생활권 동네 코드", example = "4127310500")
        @Size(min = 1, max = 20) String regionCode,

        @Schema(description = "출생 연도", example = "2007")
        @Min(1900) Integer birthYear,

        @Schema(description = "랭킹에 표시할 닉네임 (1~20자). 빈 문자열을 보내면 설정을 지우고 닉네임을 표시", example = "선우")
        @Size(max = 20) String rankingNickname
) {}
