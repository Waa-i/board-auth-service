package com.example.board.auth.service.impl;

import com.example.board.auth.dto.response.ParsedJwt;
import com.example.board.auth.exception.ExpiredTokenException;
import com.example.board.auth.exception.InvalidTokenException;
import com.example.board.auth.exception.InvalidTokenTypeException;
import com.example.board.auth.service.AuthTokenParser;
import com.example.board.auth.utils.TokenType;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtTokenParser implements AuthTokenParser {
    private final JwtParser jwtParser;

    @Override
    public ParsedJwt parse(String token) {
        try {
            var claims = jwtParser.parseSignedClaims(token).getPayload();
            var type = claims.get("type", String.class);
            if(type == null || type.isBlank()) {
                throw new InvalidTokenTypeException("유효하지 않은 토큰 타입.");
            }

            var tokenType = TokenType.valueOf(type);
            return switch (tokenType) {
                case ACCESS -> new ParsedJwt.AccessToken(Long.valueOf(claims.getSubject()), claims.getId(), claims.getIssuer(), claims.getExpiration().toInstant());
                case REFRESH -> new ParsedJwt.RefreshToken(Long.valueOf(claims.getSubject()), claims.getId(), claims.getIssuer(), claims.getExpiration().toInstant());
            };
        } catch (ExpiredJwtException e) {
            throw new ExpiredTokenException(e);
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidTokenException(e);
        }
    }
}
