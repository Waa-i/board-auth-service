package com.example.board.auth.dto.response;

public sealed interface EmailAvailabilityResult {
    record DisAllowed(String message) implements EmailAvailabilityResult {}
    record Available(String message) implements EmailAvailabilityResult {}
    record Used(String message) implements EmailAvailabilityResult {}
}
