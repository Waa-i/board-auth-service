package com.example.board.auth.service.impl;

import com.example.board.auth.service.OtpGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Slf4j
@Component
@RequiredArgsConstructor
public class SecureRandomOtpGenerator implements OtpGenerator {
    private final SecureRandom secureRandom;

    @Override
    public String generate() {
        var otp = "%06d".formatted(secureRandom.nextInt(1_000_000));
        log.info("otp 발급");
        return otp;
    }
}
