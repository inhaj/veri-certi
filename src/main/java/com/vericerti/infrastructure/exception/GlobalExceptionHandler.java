package com.vericerti.infrastructure.exception;

import com.vericerti.domain.exception.DomainException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global Exception Handler
 * Handles all Controller exceptions with consistent format
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Domain layer exceptions (Value Objects, Entities)
     */
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomainException(
            DomainException e, HttpServletRequest request) {
        log.warn("Domain exception: {} - {}", request.getRequestURI(), e.getMessage());
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("D001", e.getMessage(), request.getRequestURI()));
    }

    /**
     * Business exceptions (custom exception hierarchy)
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException e, HttpServletRequest request) {
        ErrorCode errorCode = e.getErrorCode();
        log.warn("Business exception: {} - {} [{}]", 
                request.getRequestURI(), e.getMessage(), errorCode.getCode());
        
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(new ErrorResponse(errorCode.getCode(), e.getMessage(), request.getRequestURI()));
    }

    /**
     * 비즈니스 로직 예외 (레거시 - 점진적 마이그레이션용)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(
            IllegalArgumentException e, HttpServletRequest request) {
        log.warn("Bad request: {} - {}", request.getRequestURI(), e.getMessage());
        
        return ResponseEntity.badRequest().body(
                new ErrorResponse(ErrorCode.VALIDATION_ERROR.getCode(), e.getMessage(), request.getRequestURI())
        );
    }

    /**
     * 인증 실패 (Spring Security)
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(
            BadCredentialsException e, HttpServletRequest request) {
        log.warn("Authentication failed: {} - {}", request.getRequestURI(), e.getMessage());
        
        ErrorCode errorCode = ErrorCode.INVALID_CREDENTIALS;
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(new ErrorResponse(errorCode.getCode(), errorCode.getMessage(), request.getRequestURI()));
    }

    /**
     * 접근 거부
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(
            AccessDeniedException e, HttpServletRequest request) {
        log.warn("Access denied: {} - {}", request.getRequestURI(), e.getMessage());
        
        ErrorCode errorCode = ErrorCode.ACCESS_DENIED;
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(new ErrorResponse(errorCode.getCode(), errorCode.getMessage(), request.getRequestURI()));
    }

    /**
     * Validation 예외 (@Valid 실패)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException e, HttpServletRequest request) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");

        log.warn("Validation error: {} - {}", request.getRequestURI(), message);
        
        return ResponseEntity.badRequest().body(
                new ErrorResponse(ErrorCode.VALIDATION_ERROR.getCode(), message, request.getRequestURI())
        );
    }

    /**
     * SelfValidating 검증 실패
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException e, HttpServletRequest request) {
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .findFirst()
                .orElse("Validation failed");

        log.warn("Constraint violation: {} - {}", request.getRequestURI(), message);
        
        return ResponseEntity.badRequest().body(
                new ErrorResponse(ErrorCode.VALIDATION_ERROR.getCode(), message, request.getRequestURI())
        );
    }

    /**
     * 기타 모든 예외 (예상치 못한 에러)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(
            Exception e, HttpServletRequest request) {
        log.error("Unexpected error: {} - {}", request.getRequestURI(), e.getMessage(), e);
        
        ErrorCode errorCode = ErrorCode.INTERNAL_ERROR;
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(new ErrorResponse(errorCode.getCode(), errorCode.getMessage(), request.getRequestURI()));
    }
}

