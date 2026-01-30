package com.example.board.auth.repository;

import com.example.board.auth.entity.OAuth2RegisteredClient;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface OAuth2RegisteredClientRepository extends CrudRepository<OAuth2RegisteredClient, String> {
    Optional<OAuth2RegisteredClient> findByClientId(String clientId);
    List<OAuth2RegisteredClient> findAll();
}
