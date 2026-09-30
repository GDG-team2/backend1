package com.walkmission.global.error;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    // Common
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 경로를 찾을 수 없습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 HTTP 메서드입니다."),
    DATA_CONFLICT(HttpStatus.CONFLICT, "요청이 기존 데이터와 충돌합니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),

    // Auth
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "액세스 토큰이 만료되었습니다. 토큰을 재발급해 주세요."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 유효하지 않거나 만료되었습니다. 다시 로그인해 주세요."),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 가입된 이메일입니다."),
    INVALID_PASSWORD_FORMAT(HttpStatus.BAD_REQUEST, "비밀번호는 영문과 숫자를 포함해 8자 이상이어야 합니다."),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),

    // Mission
    MISSION_NOT_FOUND(HttpStatus.NOT_FOUND, "미션을 찾을 수 없습니다."),
    NO_NEARBY_PLACE(HttpStatus.NOT_FOUND, "주변에 추천할 장소가 없습니다."),
    PLACE_SEARCH_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "장소 검색에 실패했습니다. 잠시 후 다시 시도해 주세요."),
    ACTIVE_MISSION_EXISTS(HttpStatus.CONFLICT, "이미 진행 중인 미션이 있습니다."),
    INVALID_MISSION_STATUS(HttpStatus.CONFLICT, "현재 미션 상태에서는 요청을 처리할 수 없습니다."),
    NOT_ENOUGH_DISTANCE(HttpStatus.BAD_REQUEST, "목적지 반경 80m 이내에 도착하지 않았습니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() { return status; }
    public String getMessage() { return message; }
}
