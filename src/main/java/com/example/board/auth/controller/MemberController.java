package com.example.board.auth.controller;

import com.example.board.auth.annotation.SignUpProof;
import com.example.board.auth.dto.command.EmailVerificationVerifyCommand;
import com.example.board.auth.dto.command.MemberSignUpCommand;
import com.example.board.auth.dto.command.EmailVerificationSendCommand;
import com.example.board.auth.dto.request.EmailVerificationSendRequest;
import com.example.board.auth.dto.request.EmailVerificationVerifyRequest;
import com.example.board.auth.dto.request.MemberSignUpRequest;
import com.example.board.auth.dto.response.*;
import com.example.board.auth.service.EmailVerificationService;
import com.example.board.auth.service.MemberService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;
    private final EmailVerificationService emailVerificationService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signUp(@SignUpProof String token, @Valid @RequestBody MemberSignUpRequest request) {
        var result = memberService.signUp(new MemberSignUpCommand(request.username(), request.password(), request.email(), request.nickname(), token));

        return switch (result) {
            case SignUpResult.EmailVerificationExpired _ -> {
                var code = EmailVerificationErrorCode.SIGNUP_PROOF_TOKEN_EXPIRED;
                yield ResponseEntity.status(code.getHttpStatus())
                        .body(ApiResponse.error(code));
            }
            case SignUpResult.EmailMismatch _ -> {
                var code = EmailVerificationErrorCode.EMAIL_MISMATCH;
                yield ResponseEntity.status(code.getHttpStatus())
                        .body(ApiResponse.error(code));
            }
            case SignUpResult.Success _ -> {
                var code = MemberCredentialSuccessCode.CREDENTIAL_CREATED;
                yield ResponseEntity.status(code.getHttpStatus())
                        .body(ApiResponse.success(code));
            }
            case SignUpResult.UsernameDuplicate _ -> {
                var code = MemberCredentialErrorCode.USERNAME_DUPLICATED;
                yield ResponseEntity.status(code.getHttpStatus())
                        .body(ApiResponse.error(code));
            }
            case SignUpResult.EmailDuplicate _ -> {
                var code = MemberCredentialErrorCode.EMAIL_DUPLICATED;
                yield ResponseEntity.status(code.getHttpStatus())
                        .body(ApiResponse.error(code));
            }
            case SignUpResult.NicknameDuplicate _ -> {
                var code = MemberCredentialErrorCode.NICKNAME_DUPLICATED;
                yield ResponseEntity.status(code.getHttpStatus())
                        .body(ApiResponse.error(code));
            }
            case SignUpResult.ValidationFailed _ -> {
                var code = CommonErrorCode.VALIDATION_FAILED;
                yield ResponseEntity.status(code.getHttpStatus())
                        .body(ApiResponse.error(code));
            }
            case SignUpResult.CompensationFailed _, SignUpResult.InternalError _ -> {
                var code = CommonErrorCode.INTERNAL_SERVER_ERROR;
                yield ResponseEntity.status(code.getHttpStatus())
                        .body(ApiResponse.error(code));
            }
        };
    }

    @GetMapping("/members/availability/username")
    public ResponseEntity<ApiResponse<AvailabilityData>> checkUsernameAvailability(
            @NotBlank(message = "아이디를 입력해주세요.")
            @Size(min = 5, max = 20, message = "아이디는 5~20자입니다.")
            @Pattern(
                    regexp = "^(?=.*[a-z])[a-z0-9]+$",
                    message = "아이디는 영문 소문자와 숫자만 가능하며 영문은 필수입니다."
            )
            @RequestParam
            String username) {
        var code = MemberCredentialSuccessCode.USERNAME_VERIFIED;
        if(memberService.isUsernameAvailable(username)) {
            return ResponseEntity.status(code.getHttpStatus())
                    .body(ApiResponse.success(code, AvailabilityData.ok("사용 가능한 아이디입니다.")));
        }
        return ResponseEntity.status(code.getHttpStatus())
                .body(ApiResponse.success(code, AvailabilityData.no("이미 사용 중인 아이디입니다.")));
    }

    @GetMapping("/members/availability/email")
    public ResponseEntity<ApiResponse<AvailabilityData>> checkEmailAvailability(
            @NotBlank(message = "이메일을 입력해주세요.")
            @Email(message = "이메일 형식이 올바르지 않습니다.")
            @RequestParam
            String email) {
        var code = MemberCredentialSuccessCode.EMAIL_VERIFIED;
        var result = memberService.isEmailAvailable(email);

        return switch (result) {
            case VerifyEmailResult.DisAllowed(var message) ->
                ResponseEntity.status(code.getHttpStatus())
                        .body(ApiResponse.success(code, AvailabilityData.no(message)));

            case VerifyEmailResult.Used(var message) ->
                ResponseEntity.status(code.getHttpStatus())
                        .body(ApiResponse.success(code, AvailabilityData.no(message)));

            case VerifyEmailResult.Available(var message) ->
                ResponseEntity.status(code.getHttpStatus())
                        .body(ApiResponse.success(code, AvailabilityData.ok(message)));
        };
    }

    @PostMapping("/email-verifications")
    public ResponseEntity<ApiResponse<EmailVerificationSendResponse>> sendOtp(@RequestBody EmailVerificationSendRequest request) {
        var result = emailVerificationService.sendEmailOtp(new EmailVerificationSendCommand(request.email()));
        var code = EmailVerificationSuccessCode.OTP_SENT;
        return ResponseEntity.status(code.getHttpStatus())
                .body(ApiResponse.success(code, result));
    }

    @PostMapping("/email-verifications/verify")
    public ResponseEntity<ApiResponse<EmailVerificationVerifyResponse>> verifyOtp(@RequestBody EmailVerificationVerifyRequest request) {
        var result = emailVerificationService.verifyOtp(new EmailVerificationVerifyCommand(request.email(), request.otp()));

        return switch (result) {
            case SignUpEmailVerificationResult.Success(var response) -> {
                var code = EmailVerificationSuccessCode.EMAIL_VERIFIED;
                yield ResponseEntity.status(code.getHttpStatus())
                        .body(ApiResponse.success(code, response));
            }
            case SignUpEmailVerificationResult.Expired _ -> {
                var code = EmailVerificationErrorCode.OTP_EXPIRED;
                yield ResponseEntity.status(code.getHttpStatus())
                        .body(ApiResponse.error(code));
            }
            case SignUpEmailVerificationResult.Invalid _ -> {
                var code = EmailVerificationErrorCode.OTP_INVALID;
                yield ResponseEntity.status(code.getHttpStatus())
                        .body(ApiResponse.error(code));
            }
        };
    }
}
