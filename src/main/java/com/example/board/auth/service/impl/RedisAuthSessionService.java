package com.example.board.auth.service.impl;

import com.example.board.auth.config.JwtTokenProperties;
import com.example.board.auth.dto.response.AuthTokens;
import com.example.board.auth.service.AuthSessionService;
import com.example.board.auth.service.AuthTokenIssuer;
import com.example.board.auth.service.JtiGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisAuthSessionService implements AuthSessionService {
    private final StringRedisTemplate stringRedisTemplate;
    private final AuthTokenIssuer authTokenIssuer;
    private final JtiGenerator jtiGenerator;
    private final JwtTokenProperties tokenProperties;

    @Override
    public AuthTokens createLoginSession(Long id, String type, String role) {
        // 리프레시 토큰 발급
        var jti = jtiGenerator.generate();
        var refresh = authTokenIssuer.issueRefreshToken(id, jti);
        // 리프레시 토큰 레디스에 저장
        var sessionKey = getLoginSessionKey(id, type);
        stringRedisTemplate.opsForValue().set(sessionKey, jti, tokenProperties.refreshTtl());
        // 액세스 토큰 발급
        var access = authTokenIssuer.issueAccessToken(id, role);
        return new AuthTokens(id, role, access, tokenProperties.accessTtl().toSeconds(), refresh, tokenProperties.refreshTtl().toSeconds());
    }

    private String getLoginSessionKey(Long id, String type) {
        return "auth:refresh:%d:%s".formatted(id, type);
    }
}
