package com.example.board.auth.dto.response;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum MemberCredentialSuccessCode implements ApiCode {
    CREDENTIAL_CREATED("AUTH_CREDENTIAL_S_001", "회원 가입", HttpStatus.CREATED),
    PASSWORD_UPDATED("AUTH_CREDENTIAL_S_002", "회원 비밀번호 변경", HttpStatus.OK),
    CREDENTIAL_DEACTIVATED("AUTH_CREDENTIAL_S_003", "회원 탈퇴", HttpStatus.OK),
    USERNAME_VERIFIED("AUTH_CREDENTIAL_S_004", "회원 아이디 사용 가능 여부 확인", HttpStatus.OK),
    EMAIL_VERIFIED("AUTH_CREDENTIAL_S_005", "회원 이메일 사용 가능 여부 확인", HttpStatus.OK)
    ;

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    MemberCredentialSuccessCode(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
