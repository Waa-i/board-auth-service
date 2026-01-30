package com.example.board.auth.dto.response;

public record AuthTokens(Long id, String role, String accessToken, long accessTokenExpiresIn, String refreshToken, long refreshTokenExpiresIn) {
}
