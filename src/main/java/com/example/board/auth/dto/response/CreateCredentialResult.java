package com.example.board.auth.dto.response;

public sealed interface CreateCredentialResult {
    record Success(Long id) implements CreateCredentialResult {}
    record UsernameDuplicate() implements CreateCredentialResult {}
    record EmailDuplicate() implements CreateCredentialResult {}
}
