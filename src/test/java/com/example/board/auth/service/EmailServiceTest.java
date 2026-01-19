package com.example.board.auth.service;

import com.example.board.auth.dto.request.MailMessage;
import com.example.board.auth.exception.MailSendFailedException;
import com.example.board.auth.service.impl.EmailServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {
    private static final String from = "example@gmail.com";
    @Mock
    private JavaMailSender javaMailSender;
    private EmailServiceImpl emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailServiceImpl(javaMailSender, from);
    }

    @Test
    @DisplayName("이메일 전송 성공")
    void send_success() {
        MailMessage mailMessage = MailMessage.builder()
                .to("testuser@gmail.com")
                .subject("이메일 테스트")
                .text("이메일 본문")
                .build();

        Mockito.doNothing().when(javaMailSender).send(Mockito.any(SimpleMailMessage.class));

        Assertions.assertDoesNotThrow(() -> emailService.send(mailMessage));

        Mockito.verify(javaMailSender, Mockito.times(1)).send(Mockito.any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("이메일 전송 실패 - MailSendException")
    void send_fail() {
        MailMessage mailMessage = MailMessage.builder()
                .to("testuser@gmail.com")
                .subject("이메일 테스트")
                .text("이메일 본문")
                .build();

        Mockito.doThrow(MailSendException.class).when(javaMailSender).send(Mockito.any(SimpleMailMessage.class));

        Assertions.assertThrows(MailSendFailedException.class, () -> emailService.send(mailMessage));

        Mockito.verify(javaMailSender, Mockito.times(1)).send(Mockito.any(SimpleMailMessage.class));
    }
}