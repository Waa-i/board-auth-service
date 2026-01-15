package com.example.board.auth.service;

import com.example.board.auth.dto.command.MemberCredentialCreateCommand;

public interface MemberCredentialTransactionService {
    Long createCredential(MemberCredentialCreateCommand command);
    void deleteCredential(Long id);
    void activateCredential(Long id);
}
