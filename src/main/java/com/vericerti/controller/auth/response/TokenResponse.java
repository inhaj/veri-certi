package com.vericerti.controller.auth.response;

public record TokenResponse(
        String accessToken,
        String refreshToken
) {}
