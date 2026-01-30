package com.example.board.auth.service;

import com.example.board.auth.dto.response.ParsedJwt;

public interface AuthTokenParser {
    ParsedJwt parse(String token);
}
