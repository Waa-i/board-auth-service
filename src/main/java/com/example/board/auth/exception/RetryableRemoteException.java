package com.example.board.auth.exception;

public class RetryableRemoteException extends RuntimeException {
    public RetryableRemoteException(Throwable cause) {
        super(cause);
    }
}
