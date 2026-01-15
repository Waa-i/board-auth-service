package com.example.board.auth.dto.command;

public record MemberCredentialCreateCommand(String username, String password, String email) {
}
