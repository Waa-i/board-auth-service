package com.example.board.auth.dto.command;

public record MemberSignUpCommand(String username, String password, String email, String nickname, String token) {
}
