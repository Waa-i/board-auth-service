package com.example.board.auth.dto.response;

public sealed interface UpdatePasswordResult {
    record Updated() implements UpdatePasswordResult {}
    record NotFound() implements UpdatePasswordResult {}
    record Dormant() implements UpdatePasswordResult {}
    record Deactivated() implements UpdatePasswordResult {}
    record IncorrectCurrentPasswordResult() implements UpdatePasswordResult {}
    record SameAsPreviousPasswordResult() implements UpdatePasswordResult {}
}