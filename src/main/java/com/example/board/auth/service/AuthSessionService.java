package com.example.board.auth.service;

import com.example.board.auth.dto.response.AuthTokens;

public interface AuthSessionService {
    AuthTokens createLoginSession(Long id, String type, String role);
}
