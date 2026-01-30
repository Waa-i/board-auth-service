package com.example.board.auth.dto.command;

public record LoginCommand(String username, String password, String type) {
}
