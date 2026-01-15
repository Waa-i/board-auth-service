package com.example.board.auth.dto.response;

public sealed interface VerifyEmailResult {
    record DisAllowed(String message) implements VerifyEmailResult {}
    record Available(String message) implements VerifyEmailResult {}
    record Used(String message) implements VerifyEmailResult {}
}
