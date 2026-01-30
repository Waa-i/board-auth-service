package com.example.board.auth.dto.response;

public sealed interface LoginResult {
    record Success(AuthTokens tokens) implements LoginResult {}
    record BadCredentials() implements LoginResult {}
    record AccountPending() implements LoginResult {}
    record AccountDormant() implements LoginResult {}
    record AccountWithdrawn() implements LoginResult {}
}
