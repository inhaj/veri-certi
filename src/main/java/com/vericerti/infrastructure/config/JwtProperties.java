package com.vericerti.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String secret;
    private long accessTokenExpiry;
    private long refreshTokenExpiry;

    // Cookie 설정
    private Cookie cookie = new Cookie();

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
