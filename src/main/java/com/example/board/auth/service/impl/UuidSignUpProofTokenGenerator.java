package com.example.board.auth.service.impl;

import com.example.board.auth.service.SignUpProofTokenGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Component
public class UuidSignUpProofTokenGenerator implements SignUpProofTokenGenerator {
    @Override
    public String generate() {
        var token = UUID.randomUUID().toString();
        log.info("이메일 인증 완료 토큰 발급");
        return token;
    }
}
