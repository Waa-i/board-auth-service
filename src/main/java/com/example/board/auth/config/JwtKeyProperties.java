package com.example.board.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "secret.jwt")
public record JwtKeyProperties(String keyId, String privateKeyPem, String publicKeyPem) {
}
