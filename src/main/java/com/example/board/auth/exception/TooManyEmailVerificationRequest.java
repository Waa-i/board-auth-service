package com.example.board.auth.exception;

import lombok.Getter;

@Getter
public class TooManyEmailVerificationRequest extends RuntimeException {
    private final long retryAfterSeconds;

    public TooManyEmailVerificationRequest(long retryAfterSeconds) {
        this.retryAfterSeconds = retryAfterSeconds;
    }
}
