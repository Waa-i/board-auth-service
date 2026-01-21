package com.example.board.auth.service;

import com.example.board.auth.repository.EmailVerificationRepository;
import com.example.board.auth.service.impl.MemberServiceImpl;
import com.example.board.auth.utils.EmailDomainPolicy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {
    @Mock
    private MemberSignUpOrchestrator memberSignUpOrchestrator;
    @Mock
    private EmailVerificationRepository emailVerificationRepository;
    @Mock
    private EmailDomainPolicy emailDomainPolicy;
    @InjectMocks
    private MemberServiceImpl memberService;

    @Test
    @DisplayName("")
    void signUp_success() {

    }
}