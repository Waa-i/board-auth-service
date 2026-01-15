package com.example.board.auth.dto.response;

public record EmailVerificationSendResponse(long otpExpiresInSeconds, long resendCooldownSeconds) {
}
