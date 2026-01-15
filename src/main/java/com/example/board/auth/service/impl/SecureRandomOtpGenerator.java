package com.example.board.auth.service.impl;

import com.example.board.auth.service.OtpGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class SecureRandomOtpGenerator implements OtpGenerator {
    private final SecureRandom secureRandom;

    @Override
    public String generate() {
        var otp = String.format("%06d", secureRandom.nextInt(1_000_000));
        log.info("otp 발급");
        return otp;
    }
}
