package com.example.board.auth.dto.response;

public sealed interface EmailVerificationSendResult {
    record Success(EmailVerificationSendResponse response) implements EmailVerificationSendResult {}
    record DisAllowedDomain() implements EmailVerificationSendResult {}
}
