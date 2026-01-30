package com.example.board.auth.config;

import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.PublicKey;
import java.time.Clock;

@Configuration(proxyBeanMethods = false)
public class JwtConfig {
    @Bean
    public JwtParser jwtParser(PublicKey publicKey) {
        return Jwts.parser().verifyWith(publicKey).build();
    }
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
