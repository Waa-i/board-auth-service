package com.example.board.auth.service.impl;

import com.example.board.auth.config.JwtTokenProperties;
import com.example.board.auth.service.AuthTokenIssuer;
import com.example.board.auth.service.JtiGenerator;
import com.example.board.auth.utils.TokenType;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.PrivateKey;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtTokenIssuer implements AuthTokenIssuer {
    private final PrivateKey privateKey;
    private final JwtTokenProperties tokenProperties;
    private final JtiGenerator jtiGenerator;
    private final Clock clock;

    @Override
    public String issueAccessToken(Long id, String role) {
        var claims = Map.ofEntries(Map.entry("type", TokenType.ACCESS.name()), Map.entry("role", role));
        return createToken(id, jtiGenerator.generate(), tokenProperties.accessTtl(), claims);
    }

    @Override
    public String issueRefreshToken(Long id, String jti) {
        var claims = Map.ofEntries(Map.entry("type", TokenType.REFRESH.name()));
        return createToken(id, jti, tokenProperties.refreshTtl(), claims);
    }

    private String createToken(Long id, String jti, Duration ttl, Map<String, String> claims) {
        var now = Instant.now(clock);
        var exp = now.plus(ttl);

        return Jwts.builder()
                .subject(String.valueOf(id))
                .id(jti)
                .issuer(tokenProperties.issuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .claims(claims)
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }
}
