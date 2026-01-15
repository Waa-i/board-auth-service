package com.example.board.auth.exception;

public class MailSendFailedException extends RuntimeException {
    public MailSendFailedException(Throwable cause) {
        super(cause);
    }
}
