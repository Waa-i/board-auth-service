package com.example.board.auth.dto.request;

public record EmailVerificationVerifyRequest(String email, String otp) {
}
