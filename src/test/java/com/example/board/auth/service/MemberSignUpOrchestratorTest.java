package com.example.board.auth.service;

import com.example.board.auth.client.MemberApiClient;
import com.example.board.auth.dto.command.MemberCredentialCreateCommand;
import com.example.board.auth.dto.command.MemberSignUpCommand;
import com.example.board.auth.dto.request.MemberProfileCreateRequest;
import com.example.board.auth.dto.response.ApiResponse;
import com.example.board.auth.dto.response.SignUpResult;
import com.example.board.auth.exception.IllegalCredentialStateException;
import com.example.board.auth.exception.IllegalMemberStatusChangeException;
import com.example.board.auth.exception.RetryableRemoteException;
import com.example.board.auth.retry.RetryTemplateRegistry;
import com.example.board.auth.retry.RetryableMethodName;
import com.example.board.auth.service.impl.MemberSignUpOrchestratorImpl;
import com.example.board.auth.utils.FeignExceptionUtils;
import feign.FeignException;
import feign.Request;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Duration;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberSignUpOrchestratorTest {
    private static final String MEMBER_CREDENTIAL_USERNAME_CONSTRAINT_NAME = "uk_member_credential_username";
    private static final String MEMBER_CREDENTIAL_EMAIL_CONSTRAINT_NAME = "uk_member_credential_email";
    @Mock
    private MemberCredentialTransactionService memberCredentialTransactionService;
    @Mock
    private MemberApiClient memberApiClient;
    @Mock
    private FeignExceptionUtils feignExceptionUtils;
    private MemberSignUpOrchestratorImpl memberSignUpOrchestrator;

    private Long id;
    private MemberSignUpCommand signUpCommand;

    @BeforeEach
    void setUp() {
        var retryTemplateRegistry = getTestRetryTemplateRegistry();
        memberSignUpOrchestrator = new MemberSignUpOrchestratorImpl(
                memberCredentialTransactionService,
                memberApiClient,
                feignExceptionUtils,
                retryTemplateRegistry
        );
        id = 1L;
        signUpCommand = new MemberSignUpCommand("testuser", "raw1234", "testuser@gmail.com", "테스트", "t1o2k3e4n");
    }

    @Test
    @DisplayName("회원 가입 오케스트레이터 성공")
    void coordinateSignUp_success() {
        var createCredentialCommand = new MemberCredentialCreateCommand(signUpCommand.username(), signUpCommand.password(), signUpCommand.email());
        var profileCreateRequest = new MemberProfileCreateRequest(signUpCommand.nickname());
        when(memberCredentialTransactionService.createCredential(createCredentialCommand)).thenReturn(id);
        when(memberApiClient.createProfile(id, profileCreateRequest)).thenReturn(new ApiResponse<>(true, "MEMBER_PROFILE_S_001", "회원 프로필 생성", null));
        doNothing().when(memberCredentialTransactionService).activateCredential(id);

        var actual = memberSignUpOrchestrator.coordinateSignUp(signUpCommand);
        assertInstanceOf(SignUpResult.Success.class, actual);

        verify(memberCredentialTransactionService, times(1)).createCredential(createCredentialCommand);
        verify(memberCredentialTransactionService, times(1)).activateCredential(id);
    }

    @Test
    @DisplayName("회원 가입 오케스트레이터 실패 - username 중복")
    void coordinateSignUp_fail_when_username_duplicate() {
        var dataIntegrityViolationEx = createDataIntegrityViolationException(MEMBER_CREDENTIAL_USERNAME_CONSTRAINT_NAME);
        when(memberCredentialTransactionService.createCredential(any(MemberCredentialCreateCommand.class))).thenThrow(dataIntegrityViolationEx);

        var actual = memberSignUpOrchestrator.coordinateSignUp(signUpCommand);
        assertInstanceOf(SignUpResult.UsernameDuplicate.class, actual);

        verify(memberCredentialTransactionService, times(1)).createCredential(any(MemberCredentialCreateCommand.class));
        verify(memberApiClient, never()).createProfile(anyLong(), any(MemberProfileCreateRequest.class));
        verify(memberCredentialTransactionService, never()).activateCredential(id);
    }

    @Test
    @DisplayName("회원 가입 오케스트레이터 실패 - email 중복")
    void coordinateSignUp_fail_when_email_duplicate() {
        var dataIntegrityViolationEx = createDataIntegrityViolationException(MEMBER_CREDENTIAL_EMAIL_CONSTRAINT_NAME);
        when(memberCredentialTransactionService.createCredential(any(MemberCredentialCreateCommand.class))).thenThrow(dataIntegrityViolationEx);

        var actual = memberSignUpOrchestrator.coordinateSignUp(signUpCommand);
        assertInstanceOf(SignUpResult.EmailDuplicate.class, actual);

        verify(memberCredentialTransactionService, times(1)).createCredential(any(MemberCredentialCreateCommand.class));
        verify(memberApiClient, never()).createProfile(anyLong(), any(MemberProfileCreateRequest.class));
        verify(memberCredentialTransactionService, never()).activateCredential(id);
    }

    @Test
    @DisplayName("회원 가입 오케스트레이터 실패 - 프로필 유효성 검사 실패")
    void coordinateSignUp_fail_when_profile_validation_failed() {
        var createCredentialCommand = new MemberCredentialCreateCommand(signUpCommand.username(), signUpCommand.password(), signUpCommand.email());
        var badRequest = new FeignException.BadRequest("bad-request", createRequest(), null, null);
        when(memberCredentialTransactionService.createCredential(createCredentialCommand)).thenReturn(id);
        when(memberApiClient.createProfile(eq(id), any(MemberProfileCreateRequest.class))).thenThrow(badRequest);
        doNothing().when(memberCredentialTransactionService).deleteCredential(id);

        var actual = memberSignUpOrchestrator.coordinateSignUp(signUpCommand);
        assertInstanceOf(SignUpResult.ValidationFailed.class, actual);

        verify(memberCredentialTransactionService, times(1)).createCredential(any(MemberCredentialCreateCommand.class));
        verify(memberApiClient, times(1)).createProfile(anyLong(), any(MemberProfileCreateRequest.class));
        verify(memberCredentialTransactionService, times(1)).deleteCredential(id);
        verify(memberCredentialTransactionService, never()).activateCredential(id);
    }

    @Test
    @DisplayName("회원 가입 오케스트레이터 실패 - nickname 중복")
    void coordinateSignUp_fail_when_nickname_duplicate() {
        var createCredentialCommand = new MemberCredentialCreateCommand(signUpCommand.username(), signUpCommand.password(), signUpCommand.email());
        var conflict = new FeignException.Conflict("conflict", createRequest(), null, null);
        when(memberCredentialTransactionService.createCredential(createCredentialCommand)).thenReturn(id);
        when(feignExceptionUtils.isDuplicateNickname(conflict)).thenReturn(true);
        when(memberApiClient.createProfile(eq(id), any(MemberProfileCreateRequest.class))).thenThrow(conflict);
        doNothing().when(memberCredentialTransactionService).deleteCredential(id);

        var actual = memberSignUpOrchestrator.coordinateSignUp(signUpCommand);
        assertInstanceOf(SignUpResult.NicknameDuplicate.class, actual);

        verify(memberCredentialTransactionService, times(1)).createCredential(any(MemberCredentialCreateCommand.class));
        verify(feignExceptionUtils, times(1)).isDuplicateNickname(conflict);
        verify(memberApiClient, times(1)).createProfile(anyLong(), any(MemberProfileCreateRequest.class));
        verify(memberCredentialTransactionService, times(1)).deleteCredential(id);
        verify(memberCredentialTransactionService, never()).activateCredential(id);
    }

    @Test
    @DisplayName("회원 가입 오케스트레이터 실패 - 정의되지 않은 비지니스 예외")
    void coordinateSignUp_fail_when_unknown_conflict_exception() {
        var createCredentialCommand = new MemberCredentialCreateCommand(signUpCommand.username(), signUpCommand.password(), signUpCommand.email());
        var conflict = new FeignException.Conflict("conflict", createRequest(), null, null);
        when(memberCredentialTransactionService.createCredential(createCredentialCommand)).thenReturn(id);
        when(feignExceptionUtils.isDuplicateNickname(conflict)).thenReturn(false);
        when(memberApiClient.createProfile(eq(id), any(MemberProfileCreateRequest.class))).thenThrow(conflict);
        doNothing().when(memberCredentialTransactionService).deleteCredential(id);

        var actual = memberSignUpOrchestrator.coordinateSignUp(signUpCommand);
        assertInstanceOf(SignUpResult.InternalError.class, actual);

        verify(memberCredentialTransactionService, times(1)).createCredential(any(MemberCredentialCreateCommand.class));
        verify(feignExceptionUtils, times(1)).isDuplicateNickname(conflict);
        verify(memberApiClient, times(1)).createProfile(anyLong(), any(MemberProfileCreateRequest.class));
        verify(memberCredentialTransactionService, times(1)).deleteCredential(id);
        verify(memberCredentialTransactionService, never()).activateCredential(id);
    }

    @Test
    @DisplayName("회원 가입 오케스트레이터 실패 - 재시도 할 수 없는 예외 발생")
    void coordinateSignUp_fail_when_non_retryable_exception() {
        var createCredentialCommand = new MemberCredentialCreateCommand(signUpCommand.username(), signUpCommand.password(), signUpCommand.email());
        var internalServerError = new FeignException.InternalServerError("internal-server-error", createRequest(), null, null);
        when(memberCredentialTransactionService.createCredential(createCredentialCommand)).thenReturn(id);
        when(memberApiClient.createProfile(eq(id), any(MemberProfileCreateRequest.class))).thenThrow(internalServerError);
        when(memberApiClient.deleteProfile(id)).thenReturn(new ApiResponse<>(true, "MEMBER_PROFILE_S_005","회원 프로필 삭제", null ));
        doNothing().when(memberCredentialTransactionService).deleteCredential(id);

        var actual = memberSignUpOrchestrator.coordinateSignUp(signUpCommand);
        assertInstanceOf(SignUpResult.InternalError.class, actual);

        verify(memberCredentialTransactionService, times(1)).createCredential(createCredentialCommand);
        verify(memberApiClient, times(1)).createProfile(anyLong(), any(MemberProfileCreateRequest.class));
        verify(memberApiClient, times(1)).deleteProfile(id);
        verify(memberCredentialTransactionService, times(1)).deleteCredential(id);
        verify(memberCredentialTransactionService, never()).activateCredential(id);
    }

    @Test
    @DisplayName("회원 가입 오케스트레이터 성공 - 재시도 할 수 있는 예외 발생")
    void coordinateSignUp_success_when_retryable_exception() {
        var createCredentialCommand = new MemberCredentialCreateCommand(signUpCommand.username(), signUpCommand.password(), signUpCommand.email());
        var serviceUnavailable = new FeignException.ServiceUnavailable("service-unavailable", createRequest(), null, null);
        when(memberCredentialTransactionService.createCredential(createCredentialCommand)).thenReturn(id);
        when(memberApiClient.createProfile(eq(id), any(MemberProfileCreateRequest.class)))
                .thenThrow(serviceUnavailable)
                .thenThrow(serviceUnavailable)
                .thenReturn(new ApiResponse<>(true, "MEMBER_PROFILE_S_001", "회원 프로필 생성", null));
        doNothing().when(memberCredentialTransactionService).activateCredential(id);

        var actual = memberSignUpOrchestrator.coordinateSignUp(signUpCommand);
        assertInstanceOf(SignUpResult.Success.class, actual);

        verify(memberCredentialTransactionService, times(1)).createCredential(createCredentialCommand);
        verify(memberApiClient, times(3)).createProfile(anyLong(), any(MemberProfileCreateRequest.class));
        verify(memberCredentialTransactionService, times(1)).activateCredential(id);
    }
    @Test
    @DisplayName("회원 가입 오케스트레이터 실패 - 재시도 횟수 모두 소진")
    void coordinateSignUp_fail_when_retryable_exception_exhausted() {
        var createCredentialCommand = new MemberCredentialCreateCommand(signUpCommand.username(), signUpCommand.password(), signUpCommand.email());
        var serviceUnavailable = new FeignException.ServiceUnavailable("service-unavailable", createRequest(), null, null);
        when(memberCredentialTransactionService.createCredential(createCredentialCommand)).thenReturn(id);
        when(memberApiClient.createProfile(eq(id), any(MemberProfileCreateRequest.class))).thenThrow(serviceUnavailable);
        when(memberApiClient.deleteProfile(id)).thenReturn(new ApiResponse<>(true, "MEMBER_PROFILE_S_005","회원 프로필 삭제", null ));
        doNothing().when(memberCredentialTransactionService).deleteCredential(id);

        var actual = memberSignUpOrchestrator.coordinateSignUp(signUpCommand);
        assertInstanceOf(SignUpResult.InternalError.class, actual);

        verify(memberCredentialTransactionService, times(1)).createCredential(createCredentialCommand);
        verify(memberApiClient, times(4)).createProfile(anyLong(), any(MemberProfileCreateRequest.class));
        verify(memberApiClient, times(1)).deleteProfile(id);
        verify(memberCredentialTransactionService, times(1)).deleteCredential(id);
        verify(memberCredentialTransactionService, never()).activateCredential(id);
    }

    @ParameterizedTest
    @MethodSource("provideCredentialActivationExceptions")
    @DisplayName("회원 가입 오케스트레이터 실패 - 자격증명 활성화 실패 시 전체 롤백")
    void coordinateSignUp_fail_when_credential_activation_failed(Function<Long, Throwable> activateException) {
        var throwable = activateException.apply(id);
        var createCredentialCommand = new MemberCredentialCreateCommand(signUpCommand.username(), signUpCommand.password(), signUpCommand.email());
        when(memberCredentialTransactionService.createCredential(createCredentialCommand)).thenReturn(id);
        when(memberApiClient.createProfile(eq(id), any(MemberProfileCreateRequest.class))).thenReturn(new ApiResponse<>(true, "MEMBER_PROFILE_S_001", "회원 프로필 생성", null));
        doThrow(throwable).when(memberCredentialTransactionService).activateCredential(id);
        when(memberApiClient.deleteProfile(id)).thenReturn(new ApiResponse<>(true, "MEMBER_PROFILE_S_005","회원 프로필 삭제", null ));
        doNothing().when(memberCredentialTransactionService).deleteCredential(id);

        var actual = memberSignUpOrchestrator.coordinateSignUp(signUpCommand);
        assertInstanceOf(SignUpResult.InternalError.class, actual);

        verify(memberCredentialTransactionService, times(1)).createCredential(createCredentialCommand);
        verify(memberApiClient, times(1)).createProfile(anyLong(), any(MemberProfileCreateRequest.class));
        verify(memberCredentialTransactionService, times(1)).activateCredential(id);
        verify(memberApiClient, times(1)).deleteProfile(id);
        verify(memberCredentialTransactionService, times(1)).deleteCredential(id);
    }

    private RetryTemplateRegistry getTestRetryTemplateRegistry() {
        var testRetryPolicy = RetryPolicy.builder()
                .maxRetries(3)
                .delay(Duration.ofMillis(10))
                .includes(RetryableRemoteException.class)
                .build();

        var createProfileRetryTemplate = new RetryTemplate(testRetryPolicy);
        var deleteProfileRetryTemplate = new RetryTemplate(testRetryPolicy);

        Map<RetryableMethodName, RetryTemplate> map = new EnumMap<>(RetryableMethodName.class);
        map.put(RetryableMethodName.CREATE_PROFILE, createProfileRetryTemplate);
        map.put(RetryableMethodName.DELETE_PROFILE, deleteProfileRetryTemplate);

        return new RetryTemplateRegistry(map);
    }

    private DataIntegrityViolationException createDataIntegrityViolationException(String constraintName) {
        ConstraintViolationException cve = new ConstraintViolationException("msg", null, constraintName);
        return new DataIntegrityViolationException("msg", cve);
    }

    private Request createRequest() {
        return Request.create(Request.HttpMethod.PUT, "url", Collections.emptyMap(), null, null, null);
    }

    static Stream<Function<Long, Throwable>> provideCredentialActivationExceptions() {
        return Stream.of(
                _ -> new IllegalMemberStatusChangeException("ACTIVE 상태로 변경할 수 없습니다."),
                IllegalCredentialStateException::new);
    }
}