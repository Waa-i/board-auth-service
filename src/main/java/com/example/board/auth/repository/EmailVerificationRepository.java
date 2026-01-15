package com.example.board.auth.repository;

public interface EmailVerificationRepository {
    void saveSignUpOtp(String email, String otp);
    void saveSignUpProof(String token, String email);
    String getOtp(String email);
    void deleteOtp(String email);
    String consumeSignUpProof(String token);
}
