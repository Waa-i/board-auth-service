package com.example.board.auth.dto.response;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum LoginSuccessCode implements ApiCode {
    LOGIN_SUCCESS("AUTH_LOGIN_S_001", "로그인 성공", HttpStatus.OK)
    ;

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    LoginSuccessCode(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
