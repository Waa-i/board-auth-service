package com.example.board.auth.service.impl;

import com.example.board.auth.config.JwtTokenProperties;
import com.example.board.auth.dto.command.OAuth2RegisteredClientCommand;
import com.example.board.auth.dto.response.OAuth2RegisteredClientResponse;
import com.example.board.auth.entity.OAuth2RegisteredClient;
import com.example.board.auth.repository.OAuth2RegisteredClientRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class OAuth2RegisteredClientService {
    private final PasswordEncoder bCryptPasswordEncoder;
    private final OAuth2RegisteredClientRepository oAuth2RegisteredClientRepository;
    private final JwtTokenProperties jwtTokenProperties;
    private final JsonMapper jsonMapper;

    public OAuth2RegisteredClientService(PasswordEncoder bCryptPasswordEncoder, OAuth2RegisteredClientRepository oAuth2RegisteredClientRepository, JwtTokenProperties jwtTokenProperties, @Qualifier("registeredClientJsonMapper") JsonMapper jsonMapper) {
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.oAuth2RegisteredClientRepository = oAuth2RegisteredClientRepository;
        this.jwtTokenProperties = jwtTokenProperties;
        this.jsonMapper = jsonMapper;
    }

    public void register(OAuth2RegisteredClientCommand command) {
        OAuth2RegisteredClient client = new OAuth2RegisteredClient();
        client.setId(UUID.randomUUID().toString());
        client.setClientId(UUID.randomUUID().toString());
        client.setClientIdIssuedAt(Instant.now());

        client.setClientSecret(bCryptPasswordEncoder.encode(command.clientSecret()));
        client.setClientAuthenticationMethods(ClientAuthenticationMethod.CLIENT_SECRET_BASIC.getValue());
        client.setAuthorizationGrantTypes(String.join(",", AuthorizationGrantType.REFRESH_TOKEN.getValue(), AuthorizationGrantType.AUTHORIZATION_CODE.getValue()));
        client.setClientName(command.clientName());
        client.setRedirectUris(command.redirectUris());
        client.setScopes(OidcScopes.OPENID);

        ClientSettings clientSettings = ClientSettings.builder()
                .requireAuthorizationConsent(false)
                .requireProofKey(false)
                .build();

        TokenSettings tokenSettings = TokenSettings.builder()
                .reuseRefreshTokens(false)
                .accessTokenFormat(OAuth2TokenFormat.SELF_CONTAINED)
                .accessTokenTimeToLive(jwtTokenProperties.accessTtl())
                .refreshTokenTimeToLive(jwtTokenProperties.refreshTtl())
                .build();

        client.setClientSettings(jsonMapper.writeValueAsString(clientSettings.getSettings()));
        client.setTokenSettings(jsonMapper.writeValueAsString(tokenSettings.getSettings()));

        oAuth2RegisteredClientRepository.save(client);
        log.info("OAuth2 클라이언트 등록 완료: {}", command.clientName());
    }

    public List<OAuth2RegisteredClientResponse> getClients() {
        return oAuth2RegisteredClientRepository.findAll().stream()
                .map(client -> new OAuth2RegisteredClientResponse(client.getClientId(), client.getClientName()))
                .toList();
    }
}
