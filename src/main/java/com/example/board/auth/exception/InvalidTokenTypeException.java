package com.example.board.auth.exception;

public class InvalidTokenTypeException extends InvalidTokenException {
    public InvalidTokenTypeException(String message) {
        super(message);
    }
}
