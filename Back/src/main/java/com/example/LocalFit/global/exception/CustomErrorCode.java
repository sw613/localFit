package com.example.LocalFit.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CustomErrorCode {
    //Auth 관련 ERROR
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 존재하는 이메일입니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "이미 존재하는 닉네임입니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 일치하지 않습니다."),
    INVALID_OAUTH_PROVIDER(HttpStatus.BAD_REQUEST, "지원하지 않는 OAuth 제공자입니다."),
    OAUTH_USER_EXISTS(HttpStatus.CONFLICT, "이미 일반 회원으로 등록된 이메일입니다."),
    OAUTH_PROCESSING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "소셜 로그인 처리 중 오류가 발생했습니다."),
    OAUTH2_PASSWORD_CHANGE_NOT_ALLOWED(HttpStatus.BAD_REQUEST,"소셜 로그인 사용자는 비밀번호를 변경할 수 없습니다."),
    EMAIL_NOT_FOUND_IN_OAUTH(HttpStatus.BAD_REQUEST,"OAuth2 제공자에서 이메일을 찾을 수 없습니다."),

    NOT_AUTHENTICATED(HttpStatus.UNAUTHORIZED, "인증되지 않은 사용자입니다."),
    NOT_AUTHORIZED(HttpStatus.FORBIDDEN, "해당 리소스에 대한 권한이 없습니다."),
    GOOGLE_REVOCATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Google 계정 연동 해제 중 오류가 발생했습니다."),
    OAUTH_REVOCATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "OAuth 연동 해제 중 오류가 발생했습니다."),

    //User 관련 ERROR
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    USER_DELETION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "회원 탈퇴 처리 중 오류가 발생했습니다."),
    PASSWORD_REQUIRED(HttpStatus.BAD_REQUEST, "비밀번호가 필요합니다."),

    //Redis 관련 ERROR
    REDIS_CONNECTION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Redis 연결에 실패했습니다."),
    REFRESH_TOKEN_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "리프레시 토큰 저장에 실패했습니다."),
    REFRESH_TOKEN_READ_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "리프레시 토큰 조회에 실패했습니다."),
    REFRESH_TOKEN_DELETE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "리프레시 토큰 삭제에 실패했습니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "리프레시 토큰을 찾을 수 없습니다."),
    REFRESH_TOKEN_VALIDATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "리프레시 토큰 검증 중 오류가 발생했습니다."),
    TOKEN_VALIDATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "토큰 검증 중 오류가 발생했습니다."),
    INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 액세스 토큰입니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    REDIS_KEY_NOT_FOUND(HttpStatus.NOT_FOUND, "Redis 키를 찾을 수 없습니다."),
    REDIS_OPERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Redis 작업 수행 중 오류가 발생했습니다."),
    REDIS_TIMEOUT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Redis 작업 시간이 초과되었습니다."),

    TOKEN_BLACKLISTED(HttpStatus.UNAUTHORIZED, "블랙리스트에 등록된 토큰입니다."),
    BLACKLIST_OPERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "블랙리스트 작업 중 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String message;
}
