package com.vericerti.infrastructure.exception;

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
 * 전역 예외 처리 핸들러
 * 모든 Controller에서 발생하는 예외를 일관된 형식으로 처리
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 비즈니스 로직 예외 (잘못된 요청)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(
            IllegalArgumentException e, HttpServletRequest request) {
        log.warn("Bad request: {} - {}", request.getRequestURI(), e.getMessage());
        
        return ResponseEntity.badRequest().body(
                new ErrorResponse("BAD_REQUEST", e.getMessage(), request.getRequestURI())
        );
    }

    /**
     * 인증 실패
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(
            BadCredentialsException e, HttpServletRequest request) {
        log.warn("Authentication failed: {} - {}", request.getRequestURI(), e.getMessage());
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new ErrorResponse("UNAUTHORIZED", "Invalid credentials", request.getRequestURI())
        );
    }

    /**
     * 접근 거부
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(
            AccessDeniedException e, HttpServletRequest request) {
        log.warn("Access denied: {} - {}", request.getRequestURI(), e.getMessage());
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                new ErrorResponse("FORBIDDEN", "Access denied", request.getRequestURI())
        );
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
                new ErrorResponse("VALIDATION_ERROR", message, request.getRequestURI())
        );
    }

    /**
     * SelfValidating 검증 실패 (ConstraintViolationException)
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
                new ErrorResponse("VALIDATION_ERROR", message, request.getRequestURI())
        );
    }

    /**
     * 기타 모든 예외 (예상치 못한 에러)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(
            Exception e, HttpServletRequest request) {
        log.error("Unexpected error: {} - {}", request.getRequestURI(), e.getMessage(), e);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred", request.getRequestURI())
        );
    }
}
