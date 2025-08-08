package com.sosd.sosd_backend.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // 400 Bad Request
    USER_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 가입된 사용자입니다."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "잘못된 입력값입니다.");

    // 401 Unauthorized



    // 403 Forbidden (admin용 api를 user가 요청한 경우)


    // 404 Not Found


    // 500 Internal Server Error


    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
