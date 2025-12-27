package com.vericerti.infrastructure.security;

import com.vericerti.infrastructure.config.JwtProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * JWT Token Provider for creating and validating JWT tokens.
 */
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    // Token type constants
    public static final String TOKEN_TYPE_ACCESS = "access";
    public static final String TOKEN_TYPE_REFRESH = "refresh";
    
    // Claim name constants
    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_TYPE = "type";

    private final JwtProperties jwtProperties;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(Long memberId, String email) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.getAccessTokenExpiry());

        return Jwts.builder()
                .subject(String.valueOf(memberId))
                .claim(CLAIM_EMAIL, email)
                .claim(CLAIM_TYPE, TOKEN_TYPE_ACCESS)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }

    public String createRefreshToken(Long memberId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.getRefreshTokenExpiry());

        return Jwts.builder()
                .subject(String.valueOf(memberId))
                .claim(CLAIM_TYPE, TOKEN_TYPE_REFRESH)
                .id(UUID.randomUUID().toString())  // Unique token ID (different even in same second)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Long getMemberIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return Long.parseLong(claims.getSubject());
    }

    public String getTokenType(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.get(CLAIM_TYPE, String.class);
    }
    
    /**
     * Check if the token is an access token.
     */
    public boolean isAccessToken(String token) {
        return TOKEN_TYPE_ACCESS.equals(getTokenType(token));
    }
    
    /**
     * Check if the token is a refresh token.
     */
    public boolean isRefreshToken(String token) {
        return TOKEN_TYPE_REFRESH.equals(getTokenType(token));
    }
}
