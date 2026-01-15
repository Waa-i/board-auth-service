package com.example.board.auth.exception;

public class IllegalCredentialStateException extends RuntimeException {
    public IllegalCredentialStateException(Long id) {
        super("[memberId=%d] 자격 증명이 유효하지 않습니다.".formatted(id));
    }
}
