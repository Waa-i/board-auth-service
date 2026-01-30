package com.example.board.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "security.jwt")
public record JwtTokenProperties(String issuer, Duration accessTtl, Duration refreshTtl) {
}
