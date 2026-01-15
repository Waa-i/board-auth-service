package com.example.board.auth.exception;

import com.example.board.auth.dto.response.ApiCode;
import com.example.board.auth.dto.response.ApiResponse;
import com.example.board.auth.dto.response.CommonErrorCode;
import com.example.board.auth.dto.response.EmailVerificationErrorCode;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException() {
        return handleError(CommonErrorCode.VALIDATION_FAILED);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException() {
        return handleError(CommonErrorCode.VALIDATION_FAILED);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleMalformed() {
        return handleError(CommonErrorCode.MALFORMED_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch() {
        return handleError(CommonErrorCode.MALFORMED_REQUEST);
    }

    @ExceptionHandler(UnHandleDataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnHandleDataIntegrityViolationException(UnHandleDataIntegrityViolationException e) {
        log.error("처리 되지 않은 무결성 예외 발생: {}", e.getMessage());
        return handleError(CommonErrorCode.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(TooManyEmailVerificationRequest.class)
    public ResponseEntity<ApiResponse<Void>> handleTooManyEmailVerificationRequest() {
        return handleError(EmailVerificationErrorCode.TOO_MANY_EMAIL_VERIFICATION_REQUEST);
    }

    @ExceptionHandler(MailSendFailedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMailSendFailedException() {
        return handleError(CommonErrorCode.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataAccessException() {
        return handleError(CommonErrorCode.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException() {
        return handleError(CommonErrorCode.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ApiResponse<Void>> handleError(ApiCode code) {
        return ResponseEntity.status(code.getHttpStatus()).body(ApiResponse.error(code));
    }
}
