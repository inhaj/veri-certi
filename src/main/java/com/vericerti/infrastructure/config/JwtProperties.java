package com.vericerti.infrastructure.config;

import com.vericerti.infrastructure.exception.BusinessException;
import com.vericerti.infrastructure.exception.ErrorCode;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Slf4j
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private static final int MIN_SECRET_LENGTH = 32; // 256 bits for HS256

    private String secret;
    private long accessTokenExpiry;
    private long refreshTokenExpiry;

    // Cookie settings
    private Cookie cookie = new Cookie();

    @PostConstruct
    public void validateSecret() {
        if (secret == null || secret.length() < MIN_SECRET_LENGTH) {
            log.error("JWT secret must be at least {} characters (256 bits) for HS256 algorithm. Current length: {}",
                    MIN_SECRET_LENGTH, secret == null ? 0 : secret.length());
            throw new BusinessException(ErrorCode.JWT_SECRET_INVALID);
        }
        log.info("JWT configuration validated successfully");
    }

    @Getter
    @Setter
    public static class Cookie {
        private String name = "refreshToken";
        private String path = "/api/auth/refresh";
        private String sameSite = "Strict";
        private boolean secure = true;
        private boolean httpOnly = true;
    }
}
