package com.example.board.auth.dto.response;

public sealed interface SignUpEmailVerificationResult {
    record Success(EmailVerificationVerifyResponse response) implements SignUpEmailVerificationResult {}
    record Invalid() implements SignUpEmailVerificationResult {}
    record Expired() implements SignUpEmailVerificationResult {}
}
