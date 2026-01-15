package com.example.board.auth.dto.response;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum CommonErrorCode implements ApiCode {
    MALFORMED_REQUEST("AUTH_COMMON_E_001", "잘못된 요청 형식입니다.", HttpStatus.BAD_REQUEST),
    VALIDATION_FAILED("AUTH_COMMON_E_002", "입력값이 유효하지 않습니다.", HttpStatus.BAD_REQUEST),
    INTERNAL_SERVER_ERROR("AUTH_COMMON_E_003", "현재 요청을 처리할 수 없습니다. 잠시 후 다시 시도해주세요.", HttpStatus.INTERNAL_SERVER_ERROR)
    ;

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    CommonErrorCode(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
