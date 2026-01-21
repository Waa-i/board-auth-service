package com.example.board.auth.service;

import com.example.board.auth.dto.command.EmailVerificationSendCommand;
import com.example.board.auth.dto.command.EmailVerificationVerifyCommand;
import com.example.board.auth.dto.response.EmailVerificationSendResponse;
import com.example.board.auth.dto.response.EmailVerificationSendResult;
import com.example.board.auth.dto.response.SignUpEmailVerificationResult;

public interface EmailVerificationService {
    EmailVerificationSendResult sendEmailOtp(EmailVerificationSendCommand command);
    SignUpEmailVerificationResult verifyOtp(EmailVerificationVerifyCommand command);
}
