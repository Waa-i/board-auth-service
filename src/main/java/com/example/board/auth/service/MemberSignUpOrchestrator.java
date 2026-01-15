package com.example.board.auth.service;

import com.example.board.auth.dto.command.MemberSignUpCommand;
import com.example.board.auth.dto.response.SignUpResult;

public interface MemberSignUpOrchestrator {
    SignUpResult coordinateSignUp(MemberSignUpCommand command);
}
