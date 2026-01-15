package com.example.board.auth.dto.response;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum EmailVerificationErrorCode implements ApiCode {
    OTP_INVALID("AUTH_EMAIL_VERIFICATION_E_001", "인증번호가 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    OTP_EXPIRED("AUTH_EMAIL_VERIFICATION_E_002", "인증번호가 만료되었습니다.", HttpStatus.BAD_REQUEST),
    EMAIL_MISMATCH("AUTH_EMAIL_VERIFICATION_E_003", "요청하신 이메일이 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    TOO_MANY_EMAIL_VERIFICATION_REQUEST("AUTH_EMAIL_VERIFICATION_E_004", "이메일 인증 요청이 너무 많습니다. 잠시 후 다시 시도해주세요.", HttpStatus.TOO_MANY_REQUESTS),
    SIGNUP_PROOF_TOKEN_EXPIRED("AUTH_EMAIL_VERIFICATION_E_005", "이메일 인증이 만료되었습니다. 다시 인증해주세요.", HttpStatus.BAD_REQUEST);
    ;

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    EmailVerificationErrorCode(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
