package com.example.board.auth.service;

import com.example.board.auth.config.EmailVerificationProps;
import com.example.board.auth.dto.command.EmailVerificationSendCommand;
import com.example.board.auth.dto.command.EmailVerificationVerifyCommand;
import com.example.board.auth.dto.request.MailMessage;
import com.example.board.auth.dto.response.EmailVerificationSendResponse;
import com.example.board.auth.dto.response.EmailVerificationVerifyResponse;
import com.example.board.auth.dto.response.SignUpEmailVerificationResult;
import com.example.board.auth.exception.TooManyEmailVerificationRequest;
import com.example.board.auth.repository.EmailVerificationRepository;
import com.example.board.auth.service.impl.EmailVerificationServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {
    private static final Duration OTP_TTL = Duration.ofMinutes(5);
    private static final Duration RESEND_COOL_DOWN = Duration.ofMinutes(1);
    private static final Duration SIGNUP_PROOF_TTL = Duration.ofMinutes(10);

    @Mock
    private EmailService emailService;
    @Mock
    private OtpGenerator otpGenerator;
    @Mock
    private SignUpProofTokenGenerator tokenGenerator;
    @Mock
    private EmailVerificationRepository emailVerificationRepository;
    @Mock
    private EmailVerificationProps emailVerificationProps;
    @InjectMocks
    private EmailVerificationServiceImpl emailVerificationService;

    @Test
    @DisplayName("otp 발급 및 이메일 전송 성공")
    void sendEmailOtp_success() {
        String email = "testuser@gmail.com";
        String otp = "123456";
        var command = new EmailVerificationSendCommand(email);
        var response = new EmailVerificationSendResponse(OTP_TTL.toSeconds(), RESEND_COOL_DOWN.toSeconds());
        when(otpGenerator.generate()).thenReturn(otp);
        doNothing().when(emailService).send(any(MailMessage.class));
        when(emailVerificationProps.otpTtl()).thenReturn(OTP_TTL);
        when(emailVerificationProps.resendCooldown()).thenReturn(RESEND_COOL_DOWN);

        var actual = emailVerificationService.sendEmailOtp(command);
        assertThat(actual).isEqualTo(response);

        verify(otpGenerator, times(1)).generate();
        verify(emailVerificationRepository, times(1)).saveSignUpOtp(email, otp);
        verify(emailService, times(1)).send(any(MailMessage.class));
    }

    @Test
    @DisplayName("이메일 전송 실패 - 재전송 대기시간")
    void sendEmailOtp_fail_resend_cooldown() {
        String email = "testuser@gmail.com";
        String otp = "123456";
        when(otpGenerator.generate()).thenReturn(otp);
        doThrow(TooManyEmailVerificationRequest.class).when(emailVerificationRepository).saveSignUpOtp(email, otp);

        assertThatThrownBy(() -> emailVerificationService.sendEmailOtp(new EmailVerificationSendCommand(email)))
                .isInstanceOf(TooManyEmailVerificationRequest.class);

        verify(otpGenerator, times(1)).generate();
        verify(emailService, never()).send(any(MailMessage.class));
    }

    @Test
    @DisplayName("otp 검증 성공")
    void verifyOtp_success() {
        String email = "testuser@gmail.com";
        String otp = "123456";
        String token = "abcde";
        var command = new EmailVerificationVerifyCommand(email, otp);
        var response = new EmailVerificationVerifyResponse(token, SIGNUP_PROOF_TTL.toSeconds());
        var result = new SignUpEmailVerificationResult.Success(response);
        when(emailVerificationRepository.getOtp(email)).thenReturn(otp);
        when(tokenGenerator.generate()).thenReturn(token);
        doNothing().when(emailVerificationRepository).saveSignUpProof(token, email);
        doNothing().when(emailVerificationRepository).deleteOtp(email);
        when(emailVerificationProps.signupProofTtl()).thenReturn(SIGNUP_PROOF_TTL);

        var actual = emailVerificationService.verifyOtp(command);
        assertThat(actual).isEqualTo(result);

        verify(emailVerificationRepository, times(1)).getOtp(email);
        verify(tokenGenerator, times(1)).generate();
        verify(emailVerificationRepository, times(1)).saveSignUpProof(token, email);
        verify(emailVerificationRepository, times(1)).deleteOtp(email);
    }

    @Test
    @DisplayName("otp 검증 실패 - otp 유효 기간이 지난 경우")
    void verifyOtp_fail_expired() {
        String email = "testuser@gmail.com";
        String otp = "123456";
        var command = new EmailVerificationVerifyCommand(email, otp);
        var result = new SignUpEmailVerificationResult.Expired();
        when(emailVerificationRepository.getOtp(email)).thenReturn(null);

        var actual = emailVerificationService.verifyOtp(command);
        assertThat(actual).isEqualTo(result);

        verify(emailVerificationRepository, times(1)).getOtp(email);
        verify(tokenGenerator, never()).generate();
        verify(emailVerificationRepository, never()).saveSignUpProof(Mockito.anyString(), Mockito.anyString());
        verify(emailVerificationRepository, never()).deleteOtp(email);
    }

    @Test
    @DisplayName("otp 검증 실패 - 사용자가 잘못된 otp를 보낸 경우")
    void verifyOtp_fail_mismatch() {
        String email = "testuser@gmail.com";
        String requestOtp = "123456";
        String storedOtp = "654321";
        var command = new EmailVerificationVerifyCommand(email, requestOtp);
        var result = new SignUpEmailVerificationResult.Invalid();
        when(emailVerificationRepository.getOtp(email)).thenReturn(storedOtp);

        var actual = emailVerificationService.verifyOtp(command);
        assertThat(actual).isEqualTo(result);

        verify(emailVerificationRepository, times(1)).getOtp(email);
        verify(tokenGenerator, never()).generate();
        verify(emailVerificationRepository, never()).saveSignUpProof(Mockito.anyString(), Mockito.anyString());
        verify(emailVerificationRepository, never()).deleteOtp(email);
    }
}