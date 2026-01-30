package com.example.board.auth.dto.response;

import java.time.Instant;

public sealed interface ParsedJwt {
    Long memberId();
    String jti();
    String issuer();
    Instant expiration();
    record AccessToken(Long memberId, String jti, String issuer, Instant expiration) implements ParsedJwt {}
    record RefreshToken(Long memberId, String jti, String issuer, Instant expiration) implements ParsedJwt {}
}
