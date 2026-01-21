package com.example.board.auth.service.impl;

import com.example.board.auth.dto.command.MemberPasswordUpdateCommand;
import com.example.board.auth.dto.command.MemberSignUpCommand;
import com.example.board.auth.dto.response.DeactivateResult;
import com.example.board.auth.dto.response.SignUpResult;
import com.example.board.auth.dto.response.UpdatePasswordResult;
import com.example.board.auth.dto.response.EmailAvailabilityResult;
import com.example.board.auth.repository.EmailVerificationRepository;
import com.example.board.auth.repository.MemberCredentialRepository;
import com.example.board.auth.service.MemberService;
import com.example.board.auth.service.MemberSignUpOrchestrator;
import com.example.board.auth.utils.EmailDomainPolicy;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {
    private final MemberCredentialRepository memberCredentialRepository;
    private final MemberSignUpOrchestrator memberSignUpOrchestrator;
    private final EmailVerificationRepository emailVerificationRepository;
    private final EmailDomainPolicy emailDomainPolicy;

    @Override
    public SignUpResult signUp(MemberSignUpCommand command) {
        // email 도메인 제약 조건 체크
        if(!emailDomainPolicy.isDomainAllowed(command.email())) {
            return new SignUpResult.DisAllowedDomain();
        }
        // 회원가입 폼에서 인증된 이메일과 일치하는지 이메일 검증
        var storedEmail = emailVerificationRepository.consumeSignUpProof(command.token());
        if (storedEmail == null || storedEmail.isBlank()) {
            log.warn("이메일 검증 유효기간 만료. 저장된 이메일: {}, 요청 이메일: {}", storedEmail, command.email());
            return new SignUpResult.EmailVerificationExpired();
        }
        if(!storedEmail.equals(command.email())) {
            log.warn("회원가입 폼에서 인증된 이메일과 현재 요청 이메일 불일치. 저장된 이메일: {}, 요청 이메일: {}", storedEmail, command.email());
            return new SignUpResult.EmailMismatch();
        }
        return memberSignUpOrchestrator.coordinateSignUp(command);
    }

    @Override
    @Transactional
    public UpdatePasswordResult updatePassword(Long id, MemberPasswordUpdateCommand command) {
        return null;
    }

    @Override
    @Transactional
    public DeactivateResult deactivateMember(Long id) {
        return null;
    }

    @Override
    public boolean isUsernameAvailable(String username) {
        return !memberCredentialRepository.existsByUsername(username);
    }

    @Override
    public EmailAvailabilityResult isEmailAvailable(String email) {
        if(!emailDomainPolicy.isDomainAllowed(email)) {
            return new EmailAvailabilityResult.DisAllowed("지메일과 네이버메일만 사용할 수 있습니다.");
        }
        if(memberCredentialRepository.existsByEmail(email)) {
            return new EmailAvailabilityResult.Used("이미 사용 중인 이메일입니다.");
        }
        return new EmailAvailabilityResult.Available("사용 가능한 이메일입니다.");
    }
}