package com.example.board.auth.exception;

public class IllegalMemberStatusChangeException extends RuntimeException {
    public IllegalMemberStatusChangeException(String message) {
        super(message);
    }
}
