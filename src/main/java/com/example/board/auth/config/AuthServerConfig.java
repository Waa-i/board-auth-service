package com.example.board.auth.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Configuration
@RequiredArgsConstructor
public class AuthServerConfig {
    private final JwtKeyProperties jwtKeyProperties;

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
                .oauth2AuthorizationServer(authorizationServer -> {
                            http.securityMatcher(authorizationServer.getEndpointsMatcher());
                            authorizationServer.oidc(Customizer.withDefaults());
                        }
                )
                .authorizeHttpRequests(authorize ->
                        authorize.anyRequest().authenticated()
                )
                .exceptionHandling(exceptions ->
                        exceptions.defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint("/auth/login"),
                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                        )
                );

        return http.build();
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
                .authorizationEndpoint("/auth/oauth2/authorize")
                .tokenEndpoint("/auth/oauth2/token")
                .tokenIntrospectionEndpoint("/auth/oauth2/introspect")
                .tokenRevocationEndpoint("/auth/oauth2/revoke")
                .jwkSetEndpoint("/auth/oauth2/jwks")
                .oidcUserInfoEndpoint("/auth/userinfo")
                .oidcLogoutEndpoint("/auth/connect/logout")
                .build();
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource(PublicKey publicKey, PrivateKey privateKey) {
        RSAKey rsaKey = new RSAKey.Builder((RSAPublicKey) publicKey)
                .privateKey((RSAPrivateKey) privateKey)
                .keyID(jwtKeyProperties.keyId())
                .build();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return new ImmutableJWKSet<>(jwkSet);
    }

    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }
}
