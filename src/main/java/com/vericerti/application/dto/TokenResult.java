package com.vericerti.application.dto;
public record TokenResult(
        String accessToken,
        String refreshToken
) {}
