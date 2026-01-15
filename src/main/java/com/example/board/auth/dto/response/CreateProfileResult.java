package com.example.board.auth.dto.response;

public sealed interface CreateProfileResult {
    record Success() implements CreateProfileResult {}
    record NicknameDuplicate() implements CreateProfileResult {}
    record ValidationFailed() implements CreateProfileResult {}
    record UnknownBusinessConflict() implements CreateProfileResult {}
    record RetryFailed() implements CreateProfileResult {}
    record InternalError() implements CreateProfileResult {}
}
