package com.vericerti.infrastructure.security;

import com.vericerti.infrastructure.config.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final StringRedisTemplate redisTemplate;
    private final JwtProperties jwtProperties;

    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";

    public void saveRefreshToken(Long memberId, String refreshToken) {
        String key = REFRESH_TOKEN_PREFIX + memberId;
        String tokenHash = hashToken(refreshToken);
        long ttlMs = jwtProperties.getRefreshTokenExpiry();
        redisTemplate.opsForValue().set(key, tokenHash, Duration.ofMillis(ttlMs));
    }

    public boolean validateAndRotate(Long memberId, String oldToken, String newToken) {
        String key = REFRESH_TOKEN_PREFIX + memberId;
        String storedHash = redisTemplate.opsForValue().get(key);
        String oldHash = hashToken(oldToken);

        // Reuse Detection: 이미 무효화된 토큰 사용 시
        if (storedHash == null || !storedHash.equals(oldHash)) {
            // 전체 세션 무효화
            redisTemplate.delete(key);
            return false;
        }

        // Rotation: 새 토큰으로 교체
        String newHash = hashToken(newToken);
        long ttlMs = jwtProperties.getRefreshTokenExpiry();
        redisTemplate.opsForValue().set(key, newHash, Duration.ofMillis(ttlMs));
        return true;
    }

    public void deleteRefreshToken(Long memberId) {
        String key = REFRESH_TOKEN_PREFIX + memberId;
        redisTemplate.delete(key);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes());
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
