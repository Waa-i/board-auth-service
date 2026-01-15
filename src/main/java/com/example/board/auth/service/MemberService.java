package com.example.board.auth.service;

import com.example.board.auth.dto.command.MemberPasswordUpdateCommand;
import com.example.board.auth.dto.command.MemberSignUpCommand;
import com.example.board.auth.dto.response.DeactivateResult;
import com.example.board.auth.dto.response.SignUpResult;
import com.example.board.auth.dto.response.UpdatePasswordResult;
import com.example.board.auth.dto.response.VerifyEmailResult;

public interface MemberService {
    // 회원 가입 메서드
    SignUpResult signUp(MemberSignUpCommand command);
    // 비밀번호 수정 메서드(본인만 가능)
    UpdatePasswordResult updatePassword(Long id, MemberPasswordUpdateCommand command);
    // 회원 탈퇴 메서드(본인만 가능)
    DeactivateResult deactivateMember(Long id);
    // 아이디 사용 가능 확인 메서드
    boolean isUsernameAvailable(String username);
    // 이메일 사용 가능 확인 메서드
    VerifyEmailResult isEmailAvailable(String email);
}
