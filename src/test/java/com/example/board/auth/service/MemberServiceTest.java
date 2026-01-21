package com.example.board.auth.service;

import com.example.board.auth.dto.command.MemberSignUpCommand;
import com.example.board.auth.dto.response.SignUpResult;
import com.example.board.auth.repository.EmailVerificationRepository;
import com.example.board.auth.service.impl.MemberServiceImpl;
import com.example.board.auth.utils.EmailDomainPolicy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.*;

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
    @DisplayName("회원 가입 성공")
    void signUp_success() {
        var username = "testuser";
        var rawPassword = "raw1234";
        var email = "testuser@gmail.com";
        var nickname = "테스트";
        var token = "t1o2k3e4n";
        var command = new MemberSignUpCommand(username, rawPassword, email, nickname, token);
        when(emailDomainPolicy.isDomainAllowed(email)).thenReturn(true);
        when(emailVerificationRepository.consumeSignUpProof(token)).thenReturn(email);
        when(memberSignUpOrchestrator.coordinateSignUp(command)).thenReturn(new SignUpResult.Success());

        var actual = memberService.signUp(command);
        assertInstanceOf(SignUpResult.Success.class, actual);

        verify(emailDomainPolicy, times(1)).isDomainAllowed(email);
        verify(emailVerificationRepository, times(1)).consumeSignUpProof(token);
        verify(memberSignUpOrchestrator, times(1)).coordinateSignUp(command);
    }

    @Test
    @DisplayName("회원 가입 실패 - 이메일 도메인 제약 조건 위반")
    void signUp_fail_when_domain_constraint_disallowed() {
        var username = "testuser";
        var rawPassword = "raw1234";
        var email = "testuser@gmail.com";
        var nickname = "테스트";
        var token = "t1o2k3e4n";
        var command = new MemberSignUpCommand(username, rawPassword, email, nickname, token);
        when(emailDomainPolicy.isDomainAllowed(email)).thenReturn(false);

        var actual = memberService.signUp(command);
        assertInstanceOf(SignUpResult.DisAllowedDomain.class, actual);

        verify(emailDomainPolicy, times(1)).isDomainAllowed(email);
        verify(emailVerificationRepository, never()).consumeSignUpProof(token);
        verify(memberSignUpOrchestrator, never()).coordinateSignUp(command);
    }

    @Test
    @DisplayName("회원 가입 실패 - 이메일 검증 유효기간 만료")
    void signUp_fail_when_email_verification_expired() {
        var username = "testuser";
        var rawPassword = "raw1234";
        var email = "testuser@gmail.com";
        var nickname = "테스트";
        var token = "t1o2k3e4n";
        var command = new MemberSignUpCommand(username, rawPassword, email, nickname, token);
        when(emailDomainPolicy.isDomainAllowed(email)).thenReturn(true);
        when(emailVerificationRepository.consumeSignUpProof(token)).thenReturn(null);

        var actual = memberService.signUp(command);
        assertInstanceOf(SignUpResult.EmailVerificationExpired.class, actual);

        verify(emailDomainPolicy, times(1)).isDomainAllowed(email);
        verify(emailVerificationRepository, times(1)).consumeSignUpProof(token);
        verify(memberSignUpOrchestrator, never()).coordinateSignUp(command);
    }

    @Test
    @DisplayName("회원 가입 실패 - 이메일 불일치")
    void signUp_fail_when_email_mismatch() {
        var username = "testuser";
        var rawPassword = "raw1234";
        var email = "testuser@gmail.com";
        var nickname = "테스트";
        var token = "t1o2k3e4n";
        var mismatchEmail = "mismatch@gmail.com";
        var command = new MemberSignUpCommand(username, rawPassword, email, nickname, token);
        when(emailDomainPolicy.isDomainAllowed(email)).thenReturn(true);
        when(emailVerificationRepository.consumeSignUpProof(token)).thenReturn(mismatchEmail);

        var actual = memberService.signUp(command);
        assertInstanceOf(SignUpResult.EmailMismatch.class, actual);

        verify(emailDomainPolicy, times(1)).isDomainAllowed(email);
        verify(emailVerificationRepository, times(1)).consumeSignUpProof(token);
        verify(memberSignUpOrchestrator, never()).coordinateSignUp(command);
    }
}