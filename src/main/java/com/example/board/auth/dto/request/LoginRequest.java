package com.example.board.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "아이디를 입력해주세요.")
        @Size(min = 5, max = 20, message = "아이디는 5~20자입니다.")
        @Pattern(
                regexp = "^(?=.*[a-z])[a-z0-9]+$",
                message = "올바른 아이디를 입력하세요."
        )
        String username,
        @NotBlank(message = "비밀번호를 입력해주세요.")
        @Size(min = 8, max = 20, message = "비밀번호는 8~20자입니다.")
        String password
) {
}
