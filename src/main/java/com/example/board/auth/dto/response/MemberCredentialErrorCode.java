package com.example.board.auth.dto.response;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum MemberCredentialErrorCode implements ApiCode {
    NOT_FOUND("AUTH_CREDENTIAL_E_001", "존재하지 않는 계정입니다.", HttpStatus.NOT_FOUND),
    USERNAME_DUPLICATED("AUTH_CREDENTIAL_E_002", "이미 사용 중인 아이디입니다.", HttpStatus.CONFLICT),
    EMAIL_DUPLICATED("AUTH_CREDENTIAL_E_003", "이미 사용 중인 이메일입니다.", HttpStatus.CONFLICT),
    NICKNAME_DUPLICATED("AUTH_CREDENTIAL_E_004", "이미 사용 중인 닉네임입니다.", HttpStatus.CONFLICT),
    DORMANT("AUTH_CREDENTIAL_E_005", "휴면 계정입니다. 본인인증 후 이용해주세요.", HttpStatus.CONFLICT),
    DEACTIVATED("AUTH_CREDENTIAL_E_006", "탈퇴된 계정입니다.", HttpStatus.CONFLICT),
    INCORRECT_CURRENT_PASSWORD("AUTH_CREDENTIAL_E_007", "현재 비밀번호가 올바르지 않습니다.", HttpStatus.CONFLICT),
    SAME_AS_PREVIOUS_PASSWORD("AUTH_CREDENTIAL_E_008", "새 비밀번호는 이전에 사용하던 비밀번호와 달라야 합니다.", HttpStatus.CONFLICT)
    ;

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    MemberCredentialErrorCode(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
