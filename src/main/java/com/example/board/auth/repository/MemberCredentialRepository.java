package com.example.board.auth.repository;

import com.example.board.auth.entity.MemberCredential;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberCredentialRepository extends JpaRepository<MemberCredential, Long> {
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
