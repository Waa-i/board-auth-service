package com.example.board.auth.service;

public interface AuthTokenIssuer {
    String issueAccessToken(Long id, String role);
    String issueRefreshToken(Long id, String jti);
}
