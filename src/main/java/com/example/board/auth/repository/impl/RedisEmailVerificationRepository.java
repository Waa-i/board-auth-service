package com.example.board.auth.repository.impl;

import com.example.board.auth.config.EmailVerificationProps;
import com.example.board.auth.exception.TooManyEmailVerificationRequest;
import com.example.board.auth.repository.EmailVerificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Slf4j
@Repository
@RequiredArgsConstructor
public class RedisEmailVerificationRepository implements EmailVerificationRepository {
    private final StringRedisTemplate stringRedisTemplate;
    private final EmailVerificationProps emailVerificationProps;

    @Override
    public void saveSignUpOtp(String email, String otp) {
        var cooldownKey = getSignUpOtpCooldownKey(email);
        var isNewCooldownSet = stringRedisTemplate.opsForValue().setIfAbsent(cooldownKey, "1", emailVerificationProps.resendCooldown());
        if(Boolean.FALSE.equals(isNewCooldownSet)) {
            var remainSeconds = stringRedisTemplate.getExpire(cooldownKey, TimeUnit.SECONDS);
            var retryAfterSeconds = remainSeconds == null || remainSeconds < 0 ? emailVerificationProps.resendCooldown().toSeconds() : remainSeconds;
            throw new TooManyEmailVerificationRequest(retryAfterSeconds);
        }
        var otpKey = getSignUpOtpKey(email);
        stringRedisTemplate.opsForValue().set(otpKey, otp, emailVerificationProps.otpTtl());
        log.info("otp 저장");
    }

    @Override
    public void saveSignUpProof(String token, String email) {
        var signUpProofKey = getSignUpProofKey(token);
        stringRedisTemplate.opsForValue().set(signUpProofKey, email, emailVerificationProps.signupProofTtl());
        log.info("인증 완료된 이메일 저장: {}", email);
    }

    @Override
    public String getOtp(String email) {
        return stringRedisTemplate.opsForValue().get(getSignUpOtpKey(email));
    }

    @Override
    public void deleteOtp(String email) {
        stringRedisTemplate.delete(getSignUpOtpKey(email));
    }

    @Override
    public String consumeSignUpProof(String token) {
        var signUpProofKey = getSignUpProofKey(token);
        var email = stringRedisTemplate.opsForValue().getAndDelete(signUpProofKey);
        log.info("이메일 인증 완료 토큰 사용. 토큰: {}, 이메일: {}", token, email);
        return email;
    }

    private String getSignUpOtpKey(String email) {
        return "auth:signup:otp:%s".formatted(email);
    }

    private String getSignUpOtpCooldownKey(String email) {
        return "auth:signup:otp:cooldown:%s".formatted(email);
    }

    private String getSignUpProofKey(String token) {
        return "auth:signup:proof:%s".formatted(token);
    }
}
