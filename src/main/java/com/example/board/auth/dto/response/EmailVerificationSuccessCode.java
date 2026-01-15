package com.example.board.auth.dto.response;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum EmailVerificationSuccessCode implements ApiCode {
    OTP_SENT("AUTH_EMAIL_VERIFICATION_S_001", "인증번호가 이메일로 전송되었습니다.", HttpStatus.OK),
    EMAIL_VERIFIED("AUTH_EMAIL_VERIFICATION_S_002", "이메일 인증이 완료되었습니다.", HttpStatus.OK)
    ;

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    EmailVerificationSuccessCode(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
