package com.walkmission.domain.user.dto;

import com.walkmission.global.util.RegionUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "프로필 수정 요청 DTO (모든 필드 선택, 보낸 필드만 변경)")
public record UserProfileUpdateRequest(
        @Schema(description = "닉네임 (1~20자)", example = "선우")
        @Size(min = 1, max = 20) String nickname,

        @Schema(description = "동네 법정동 코드 (10자리 숫자). 랭킹은 앞 5자리(시·군·구) 단위로 묶임", example = "1174010800")
        @Pattern(regexp = RegionUtils.REGEX, message = "법정동 코드 10자리 숫자여야 합니다") String regionCode,

        @Schema(description = "출생 연도", example = "2007")
        @Min(1900) Integer birthYear,

        @Schema(description = "랭킹에 표시할 닉네임 (1~20자). 빈 문자열을 보내면 설정을 지우고 닉네임을 표시", example = "선우")
        @Size(max = 20) String rankingNickname
) {}
