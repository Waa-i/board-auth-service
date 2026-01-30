package com.example.board.auth.exception;

public class ExpiredTokenException extends RuntimeException {
    public ExpiredTokenException(Throwable cause) {
        super(cause);
    }
}
