package com.vericerti.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * 
 *   cors:
 *     allowed-origins:
 *       - domain
 */
@ConfigurationProperties(prefix = "cors")
public record CorsProperties(
    List<String> allowedOrigins
) {
    private static final List<String> DEFAULT_ORIGINS = List.of(
        "http://localhost:*", 
        "https://localhost:*"
    );
    
    public List<String> allowedOrigins() {
        return (allowedOrigins == null || allowedOrigins.isEmpty()) 
            ? DEFAULT_ORIGINS 
            : allowedOrigins;
    }
}
