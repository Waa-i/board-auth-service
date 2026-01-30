package com.example.board.auth.config;

import com.example.board.auth.utils.PemUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
public class RsaConfig {
    private final JwtKeyProperties jwtKeyProperties;

    @Bean
    public PrivateKey privateKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        return PemUtils.getPrivateKey(jwtKeyProperties.privateKeyPem());
    }

    @Bean
    public PublicKey publicKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        return PemUtils.getPublicKey(jwtKeyProperties.publicKeyPem());
    }
}
