package com.example.board.auth.dto.command;

public record OAuth2RegisteredClientCommand(String clientName, String clientSecret, String redirectUris) {
}
