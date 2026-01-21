package com.example.board.auth.dto.response;

public sealed interface SignUpResult {
    record Success() implements SignUpResult {}
    record EmailVerificationExpired() implements SignUpResult {}
    record EmailMismatch() implements SignUpResult {}
    record DisAllowedDomain() implements SignUpResult {}
    record UsernameDuplicate() implements SignUpResult {}
    record EmailDuplicate() implements SignUpResult {}
    record NicknameDuplicate() implements SignUpResult {}
    record ValidationFailed() implements SignUpResult {}
    record CompensationFailed() implements SignUpResult {}
    record InternalError() implements SignUpResult {}
}
