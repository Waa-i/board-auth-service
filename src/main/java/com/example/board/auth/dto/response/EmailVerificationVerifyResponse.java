package com.example.board.auth.dto.response;

public record EmailVerificationVerifyResponse(String token, long expiresInSeconds) {
}
