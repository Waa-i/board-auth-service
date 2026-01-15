package com.example.board.auth.service.impl;

import com.example.board.auth.dto.request.MailMessage;
import com.example.board.auth.exception.MailSendFailedException;
import com.example.board.auth.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender javaMailSender;
    private final String from;

    public EmailServiceImpl(JavaMailSender javaMailSender, @Value("${spring.mail.username}") String from) {
        this.javaMailSender = javaMailSender;
        this.from = from;
    }

    @Override
    public void send(MailMessage message) {
        try {
            var simpleMailMessage = getSimpleMailMessage(message);
            javaMailSender.send(simpleMailMessage);
            log.info("이메일 전송 완료");
        } catch (MailException e) {
            log.warn("이메일 전송 실패 to={}", message.to());
            throw new MailSendFailedException(e);
        }
    }

    private SimpleMailMessage getSimpleMailMessage(MailMessage message) {
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setTo(message.to());
        simpleMailMessage.setFrom(from);
        simpleMailMessage.setSubject(message.subject());
        simpleMailMessage.setText(message.text());

        return simpleMailMessage;
    }
}
