package com.example.board.auth.dto.response;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum LoginErrorCode implements ApiCode {
    BAD_CREDENTIALS("AUTH_LOGIN_E_001", "아이디 또는 비밀번호가 올바르지 않습니다.", HttpStatus.UNAUTHORIZED),
    ACCOUNT_DORMANT("AUTH_LOGIN_E_002", "휴면 계정입니다.", HttpStatus.FORBIDDEN),
    ACCOUNT_WITHDRAWN("AUTH_LOGIN_E_003", "탈퇴한 계정입니다.", HttpStatus.FORBIDDEN),
    ACCOUNT_PENDING("AUTH_LOGIN_E_004", "접근할 수 없는 계정입니다.", HttpStatus.FORBIDDEN)
    ;

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    LoginErrorCode(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
