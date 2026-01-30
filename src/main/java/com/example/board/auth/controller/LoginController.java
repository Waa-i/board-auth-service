package com.example.board.auth.controller;

import com.example.board.auth.annotation.DeviceType;
import com.example.board.auth.dto.command.LoginCommand;
import com.example.board.auth.dto.request.LoginRequest;
import com.example.board.auth.dto.response.*;
import com.example.board.auth.service.LoginService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class LoginController {
    private final LoginService loginService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthTokens>> login(@DeviceType String type, @Valid @RequestBody LoginRequest request) {
        var result = loginService.login(new LoginCommand(request.username(), request.password(), type));

        return switch (result) {
            case LoginResult.Success(var tokens) -> {
                var code = LoginSuccessCode.LOGIN_SUCCESS;
                yield ResponseEntity.status(code.getHttpStatus())
                        .body(ApiResponse.success(code, tokens));
            }
            case LoginResult.BadCredentials _ -> {
                var code = LoginErrorCode.BAD_CREDENTIALS;
                yield ResponseEntity.status(code.getHttpStatus())
                        .body(ApiResponse.error(code));
            }
            case LoginResult.AccountDormant _ -> {
                var code = LoginErrorCode.ACCOUNT_DORMANT;
                yield ResponseEntity.status(code.getHttpStatus())
                        .body(ApiResponse.error(code));
            }
            case LoginResult.AccountPending _ -> {
                var code = LoginErrorCode.ACCOUNT_PENDING;
                yield ResponseEntity.status(code.getHttpStatus())
                        .body(ApiResponse.error(code));
            }
            case LoginResult.AccountWithdrawn _ -> {
                var code = LoginErrorCode.ACCOUNT_WITHDRAWN;
                yield ResponseEntity.status(code.getHttpStatus())
                        .body(ApiResponse.error(code));
            }
        };
    }
}
