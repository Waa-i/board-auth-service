package com.example.board.auth.dto.response;

public sealed interface DeactivateResult {
    record Success() implements DeactivateResult {}
    record NotFound() implements DeactivateResult {}
    record AlreadyDeactivated() implements DeactivateResult {}
}