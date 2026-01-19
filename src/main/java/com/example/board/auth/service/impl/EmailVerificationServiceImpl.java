package com.example.board.auth.service.impl;

import com.example.board.auth.config.EmailVerificationProps;
import com.example.board.auth.dto.command.EmailVerificationSendCommand;
import com.example.board.auth.dto.command.EmailVerificationVerifyCommand;
import com.example.board.auth.dto.request.MailMessage;
import com.example.board.auth.dto.response.EmailVerificationSendResponse;
import com.example.board.auth.dto.response.EmailVerificationVerifyResponse;
import com.example.board.auth.dto.response.SignUpEmailVerificationResult;
import com.example.board.auth.repository.EmailVerificationRepository;
import com.example.board.auth.service.EmailService;
import com.example.board.auth.service.EmailVerificationService;
import com.example.board.auth.service.OtpGenerator;
import com.example.board.auth.service.SignUpProofTokenGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationServiceImpl implements EmailVerificationService {
    private final EmailService emailService;
    private final OtpGenerator otpGenerator;
    private final SignUpProofTokenGenerator tokenGenerator;
    private final EmailVerificationRepository emailVerificationRepository;
    private final EmailVerificationProps emailVerificationProps;

    @Override
    public EmailVerificationSendResponse sendEmailOtp(EmailVerificationSendCommand command) {
        // otp 발급
        var otp = otpGenerator.generate();
        // otp 저장
        emailVerificationRepository.saveSignUpOtp(command.email(), otp);
        // 메일 전송
        var message = buildMailMessage(command.email(), otp);
        emailService.send(message);
        return new EmailVerificationSendResponse(emailVerificationProps.otpTtl().toSeconds(), emailVerificationProps.resendCooldown().toSeconds());
    }

    @Override
    public SignUpEmailVerificationResult verifyOtp(EmailVerificationVerifyCommand command) {
        // otp 검증
        var storedOtp = emailVerificationRepository.getOtp(command.email());
        // otp 유효 기간이 지난 경우
        if(storedOtp == null || storedOtp.isBlank()) {
            return new SignUpEmailVerificationResult.Expired();
        }
        // 저장된 otp와 사용자가 보낸 otp가 다른 경우
        if(!storedOtp.equals(command.otp())) {
            return new SignUpEmailVerificationResult.Invalid();
        }
        // token 발급
        var token = tokenGenerator.generate();
        // token 발급 된 이메일 저장
        emailVerificationRepository.saveSignUpProof(token, command.email());
        // 저장된 otp와 사용자가 보낸 otp가 같은 경우 검증 했으므로 삭제
        emailVerificationRepository.deleteOtp(command.email());
        log.info("이메일 인증 완료");
        return new SignUpEmailVerificationResult.Success(new EmailVerificationVerifyResponse(token, emailVerificationProps.signupProofTtl().toSeconds()));
    }

    private MailMessage buildMailMessage(String email, String otp) {
        return MailMessage.builder()
                .to(email)
                .subject("회원가입 이메일 인증번호 안내")
                .text("""
                        회원가입을 위해 이메일 인증을 진행합니다.
                        인증 번호: %s
                        발급된 이메일 인증번호를 복사하거나 직접 입력하여 인증을 완료해주세요.
                        개인정보 보호를 위해 인증번호는 5분간 유효합니다.
                        """.formatted(otp))
                .build();
    }
}
