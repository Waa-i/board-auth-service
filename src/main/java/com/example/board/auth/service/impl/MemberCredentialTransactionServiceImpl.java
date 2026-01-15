package com.example.board.auth.service.impl;

import com.example.board.auth.dto.command.MemberCredentialCreateCommand;
import com.example.board.auth.entity.MemberCredential;
import com.example.board.auth.exception.IllegalCredentialStateException;
import com.example.board.auth.repository.MemberCredentialRepository;
import com.example.board.auth.service.MemberCredentialTransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberCredentialTransactionServiceImpl implements MemberCredentialTransactionService {
    private final MemberCredentialRepository memberCredentialRepository;
    private final PasswordEncoder bCryptPasswordEncoder;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Long createCredential(MemberCredentialCreateCommand command) {
        var credential = MemberCredential.createMember(command.username(), bCryptPasswordEncoder.encode(command.password()), command.email());
        memberCredentialRepository.saveAndFlush(credential);
        log.info("회원 자격 증명 생성: {}", credential.getId());
        return credential.getId();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteCredential(Long id) {
        memberCredentialRepository.deleteById(id);
        log.info("회원 자격 증명 삭제: {}", id);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void activateCredential(Long id) {
        var member = memberCredentialRepository.findById(id)
                .orElseThrow(() -> new IllegalCredentialStateException(id));
        member.activate();
        log.info("회원 자격 증명 활성화: {}", member.getId());
    }
}
