package com.vericerti.infrastructure.exception;

/**
 * 인증 관련 예외
 */
public class AuthenticationException extends BusinessException {
    
    public AuthenticationException(ErrorCode errorCode) {
        super(errorCode);
    }
    
    public AuthenticationException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
    
    public static AuthenticationException invalidCredentials() {
        return new AuthenticationException(ErrorCode.INVALID_CREDENTIALS);
    }
    
    public static AuthenticationException invalidToken() {
        return new AuthenticationException(ErrorCode.INVALID_TOKEN);
    }
    
    public static AuthenticationException tokenExpired() {
        return new AuthenticationException(ErrorCode.TOKEN_EXPIRED);
    }
    
    public static AuthenticationException tokenReuseDetected() {
        return new AuthenticationException(ErrorCode.TOKEN_REUSE_DETECTED);
    }
}
