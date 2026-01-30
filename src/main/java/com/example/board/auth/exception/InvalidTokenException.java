package com.example.board.auth.exception;

public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String message) {
        super(message);
    }
    public InvalidTokenException(Throwable cause) {
        super(cause);
    }
}
