package com.example.board.auth.service.impl;

import com.example.board.auth.client.MemberApiClient;
import com.example.board.auth.dto.command.MemberCredentialCreateCommand;
import com.example.board.auth.dto.command.MemberSignUpCommand;
import com.example.board.auth.dto.request.MemberProfileCreateRequest;
import com.example.board.auth.dto.response.CreateCredentialResult;
import com.example.board.auth.dto.response.CreateProfileResult;
import com.example.board.auth.dto.response.DeleteProfileResult;
import com.example.board.auth.dto.response.SignUpResult;
import com.example.board.auth.exception.IllegalCredentialStateException;
import com.example.board.auth.exception.IllegalMemberStatusChangeException;
import com.example.board.auth.exception.RetryableRemoteException;
import com.example.board.auth.exception.UnHandleDataIntegrityViolationException;
import com.example.board.auth.retry.RetryTemplateRegistry;
import com.example.board.auth.service.MemberCredentialTransactionService;
import com.example.board.auth.service.MemberSignUpOrchestrator;
import com.example.board.auth.utils.FeignExceptionUtils;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.core.retry.RetryException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import static com.example.board.auth.retry.RetryableMethodName.CREATE_PROFILE;
import static com.example.board.auth.retry.RetryableMethodName.DELETE_PROFILE;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberSignUpOrchestratorImpl implements MemberSignUpOrchestrator {
    private static final String MEMBER_CREDENTIAL_USERNAME_CONSTRAINT_NAME = "uk_member_credential_username";
    private static final String MEMBER_CREDENTIAL_EMAIL_CONSTRAINT_NAME = "uk_member_credential_email";
    private final MemberCredentialTransactionService memberCredentialTransactionService;
    private final MemberApiClient memberApiClient;
    private final FeignExceptionUtils feignExceptionUtils;
    private final RetryTemplateRegistry retryTemplateRegistry;

    public SignUpResult coordinateSignUp(MemberSignUpCommand command) {
        var credentialResult = createCredential(command);
        return switch (credentialResult) {
            case CreateCredentialResult.UsernameDuplicate _ -> new SignUpResult.UsernameDuplicate();
            case CreateCredentialResult.EmailDuplicate _ -> new SignUpResult.EmailDuplicate();
            case CreateCredentialResult.Success success -> processCredentialCreationSuccess(success.id(), command);
        };
    }

    private SignUpResult processCredentialCreationSuccess(Long memberId, MemberSignUpCommand command) {
        var createProfileResult = createProfile(memberId, command);
        return switch (createProfileResult) {
            case CreateProfileResult.NicknameDuplicate _ -> {
                // 보상 트랜잭션: 회원 서버에서 프로필이 생성되지 않았으므로 PENDING 상태의 계정 삭제
                memberCredentialTransactionService.deleteCredential(memberId);
                yield new SignUpResult.NicknameDuplicate();
            }
            case CreateProfileResult.ValidationFailed _ -> {
                // 보상 트랜잭션: 회원 서버에서 프로필이 생성되지 않았으므로 PENDING 상태의 계정 삭제
                memberCredentialTransactionService.deleteCredential(memberId);
                yield new SignUpResult.ValidationFailed();
            }
            case CreateProfileResult.UnknownBusinessConflict _ -> {
                // 보상 트랜잭션: 회원 서버에서 프로필이 생성되지 않았으므로 PENDING 상태의 계정 삭제
                memberCredentialTransactionService.deleteCredential(memberId);
                yield new SignUpResult.InternalError();
            }
            case CreateProfileResult.RetryFailed _, CreateProfileResult.InternalError _ ->
                // 보상 트랜잭션: 회원 서버에서 프로필이 생성 되었을 수도 있으므로 회원 프로필 삭제 후 자격 증명 삭제
                rollbackMemberCreation(memberId);
            case CreateProfileResult.Success _ -> processProfileCreationSuccess(memberId);
        };
    }

    private SignUpResult processProfileCreationSuccess(Long memberId) {
        try {
            memberCredentialTransactionService.activateCredential(memberId);
            return new SignUpResult.Success();
        } catch (IllegalMemberStatusChangeException | IllegalCredentialStateException _) {
            log.error("[memberId={}] 회원 프로필은 생성되었지만 회원 자격 증명 상태를 변경할 수 없습니다. ", memberId);
            // 보상 트랜잭션: 회원 서버에서 프로필이 생성 되었지만 유효한 회원 자격 증명이 존재하지 않으므로 생성된 프로필 삭제
            return rollbackMemberCreation(memberId);
        }
    }

    private SignUpResult rollbackMemberCreation(Long memberId) {
        var deleteProfileResult = deleteProfile(memberId);
        return switch (deleteProfileResult) {
            case DeleteProfileResult.InternalError _, DeleteProfileResult.RetryFailed _ -> {
                log.error("[memberId={}] 회원 프로필 보상 트랜잭션 실패", memberId);
                // 회원 프로필이 삭제 됐는지 알 수 없는 상태에서 자격 증명을 삭제하면 고아 프로필이 생길 수 있으므로 별도 처리 필요
                // DLQ 등을 이용해 후속 처리 필요
                yield new SignUpResult.CompensationFailed();
            }
            case DeleteProfileResult.Success _ -> {
                // 보상 트랜잭션: 회원 프로필 삭제 성공 -> 회원 자격 증명 삭제
                memberCredentialTransactionService.deleteCredential(memberId);
                yield new SignUpResult.InternalError();
            }
        };
    }
    // 회원 자격 증명 생성 PENDING
    private CreateCredentialResult createCredential(MemberSignUpCommand command) {
        try {
            return new CreateCredentialResult.Success(memberCredentialTransactionService.createCredential(new MemberCredentialCreateCommand(command.username(), command.password(), command.email())));
        } catch (DataIntegrityViolationException e) {
            String constraintName = findConstraintName(e);
            if(MEMBER_CREDENTIAL_USERNAME_CONSTRAINT_NAME.equals(constraintName)) {
                return new CreateCredentialResult.UsernameDuplicate();
            }
            if(MEMBER_CREDENTIAL_EMAIL_CONSTRAINT_NAME.equals(constraintName)) {
                return new CreateCredentialResult.EmailDuplicate();
            }
            throw new UnHandleDataIntegrityViolationException(e);
        }
    }
    // 회원 프로필 생성
    private CreateProfileResult createProfile(Long memberId, MemberSignUpCommand command) {
        try {
            return retryTemplateRegistry.getRetryTemplate(CREATE_PROFILE).execute(() -> {
                try {
                    memberApiClient.createProfile(memberId, new MemberProfileCreateRequest(command.nickname()));
                    return new CreateProfileResult.Success();
                } catch (FeignException.BadRequest _) {
                    return new CreateProfileResult.ValidationFailed();
                } catch (FeignException.Conflict conflict) {
                    if(feignExceptionUtils.isDuplicateNickname(conflict)) {
                        return new CreateProfileResult.NicknameDuplicate();
                    }
                    log.error("정의되지 않은 409 예외: {}", conflict.getMessage());
                    return new CreateProfileResult.UnknownBusinessConflict();
                } catch (FeignException e) {
                    if(isRetryableStatus(e.status())) {
                        throw new RetryableRemoteException(e);
                    }
                    log.error("회원 프로필 생성을 재시도할 수 없습니다. 상태: {}, 메세지: {}", e.status(), e.getMessage());
                    return new CreateProfileResult.InternalError();
                }
            });
        } catch (RetryException e) {
            log.error("[memberId={}] RETRY FAILED", memberId, e);
            return new CreateProfileResult.RetryFailed();
        }
    }

    private DeleteProfileResult deleteProfile(Long memberId) {
        try {
            return retryTemplateRegistry.getRetryTemplate(DELETE_PROFILE).execute(() -> {
                try {
                    memberApiClient.deleteProfile(memberId);
                    return new DeleteProfileResult.Success();
                } catch (FeignException e) {
                    if(isRetryableStatus(e.status())) {
                        throw new RetryableRemoteException(e);
                    }
                    log.error("회원 프로필 삭제 보상 트랜잭션을 재시도할 수 없습니다. -> 상태: {}, 메세지: {}", e.status(), e.getMessage());
                    return new DeleteProfileResult.InternalError();
                }
            });
        } catch (RetryException e) {
            log.error("[memberId={}] 회원 프로필 삭제 보상 트랜잭션 실패", memberId, e);
            return new DeleteProfileResult.RetryFailed();
        }
    }

    private boolean isRetryableStatus(int status) {
        return status == -1 || status == 429 || status == 502 || status == 503 || status == 504;
    }

    private String findConstraintName(Throwable throwable) {
        for(var cause = throwable.getCause(); cause != null; cause = cause.getCause()) {
            if(cause instanceof ConstraintViolationException cve) {
                return cve.getConstraintName();
            }
        }
        return null;
    }
}
