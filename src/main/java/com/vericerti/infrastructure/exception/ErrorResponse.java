package com.vericerti.infrastructure.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

/**
 * 통일된 에러 응답 형식
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        String code,
        String message,
        LocalDateTime timestamp,
        String path
) {
    public ErrorResponse(String code, String message) {
        this(code, message, LocalDateTime.now(), null);
    }

    public ErrorResponse(String code, String message, String path) {
        this(code, message, LocalDateTime.now(), path);
    }
}
