package com.example.board.auth.dto.command;

public record EmailVerificationVerifyCommand(String email, String otp) {
}
