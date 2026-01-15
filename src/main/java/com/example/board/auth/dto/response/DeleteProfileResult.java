package com.example.board.auth.dto.response;

public sealed interface DeleteProfileResult {
    record Success() implements DeleteProfileResult {}
    record RetryFailed() implements DeleteProfileResult {}
    record InternalError() implements DeleteProfileResult {}
}
