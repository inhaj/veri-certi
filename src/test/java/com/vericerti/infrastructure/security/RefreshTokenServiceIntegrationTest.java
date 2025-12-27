package com.vericerti.infrastructure.security;

import com.vericerti.config.BaseIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class RefreshTokenServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private static final Long MEMBER_ID = 1L;

    @BeforeEach
    void setUp() {
        // Clean up any existing tokens for test member
        refreshTokenService.deleteRefreshToken(MEMBER_ID);
    }

    @Test
    @DisplayName("saveRefreshToken - should store hashed token in Redis")
    void saveRefreshToken_shouldStoreHashedToken() {
        // given
        String refreshToken = jwtTokenProvider.createRefreshToken(MEMBER_ID);

        // when
        refreshTokenService.saveRefreshToken(MEMBER_ID, refreshToken);

        // then - validateAndRotate should work with the stored token
        String newToken = jwtTokenProvider.createRefreshToken(MEMBER_ID);
        boolean result = refreshTokenService.validateAndRotate(MEMBER_ID, refreshToken, newToken);
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("validateAndRotate - should accept valid token and rotate to new token")
    void validateAndRotate_withValidToken_shouldSucceed() {
        // given
        String originalToken = jwtTokenProvider.createRefreshToken(MEMBER_ID);
        refreshTokenService.saveRefreshToken(MEMBER_ID, originalToken);
        String newToken = jwtTokenProvider.createRefreshToken(MEMBER_ID);

        // when
        boolean result = refreshTokenService.validateAndRotate(MEMBER_ID, originalToken, newToken);

        // then
        assertThat(result).isTrue();

        // Verify the new token is now stored (old token should no longer work)
        String anotherNewToken = jwtTokenProvider.createRefreshToken(MEMBER_ID);
        boolean oldTokenResult = refreshTokenService.validateAndRotate(MEMBER_ID, originalToken, anotherNewToken);
        
        // Old token is rejected AND session is invalidated (reuse detection security measure)
        assertThat(oldTokenResult).isFalse();
        
        // Since session was invalidated by reuse attempt, new token also fails
        String yetAnotherToken = jwtTokenProvider.createRefreshToken(MEMBER_ID);
        boolean newTokenResult = refreshTokenService.validateAndRotate(MEMBER_ID, newToken, yetAnotherToken);
        assertThat(newTokenResult).isFalse(); // Session invalidated
    }

    @Test
    @DisplayName("validateAndRotate - should detect token reuse and invalidate session")
    void validateAndRotate_withReuseDetection_shouldInvalidateSession() {
        // given - User logs in and gets token A
        String tokenA = jwtTokenProvider.createRefreshToken(MEMBER_ID);
        refreshTokenService.saveRefreshToken(MEMBER_ID, tokenA);

        // User refreshes with token A, gets token B
        String tokenB = jwtTokenProvider.createRefreshToken(MEMBER_ID);
        boolean rotation1 = refreshTokenService.validateAndRotate(MEMBER_ID, tokenA, tokenB);
        assertThat(rotation1).isTrue();

        // Attacker tries to use stolen token A (already rotated out)
        String attackerToken = jwtTokenProvider.createRefreshToken(MEMBER_ID);
        boolean reuseAttempt = refreshTokenService.validateAndRotate(MEMBER_ID, tokenA, attackerToken);

        // then - Session should be invalidated
        assertThat(reuseAttempt).isFalse();

        // Even legitimate user's token B should no longer work (session invalidated)
        String legitNewToken = jwtTokenProvider.createRefreshToken(MEMBER_ID);
        boolean legitAttempt = refreshTokenService.validateAndRotate(MEMBER_ID, tokenB, legitNewToken);
        assertThat(legitAttempt).isFalse();
    }

    @Test
    @DisplayName("validateAndRotate - should reject invalid token")
    void validateAndRotate_withInvalidToken_shouldReject() {
        // given
        String validToken = jwtTokenProvider.createRefreshToken(MEMBER_ID);
        refreshTokenService.saveRefreshToken(MEMBER_ID, validToken);
        String invalidToken = jwtTokenProvider.createRefreshToken(MEMBER_ID); // Different token

        // when
        String newToken = jwtTokenProvider.createRefreshToken(MEMBER_ID);
        boolean result = refreshTokenService.validateAndRotate(MEMBER_ID, invalidToken, newToken);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("validateAndRotate - should reject when no token stored")
    void validateAndRotate_withNoStoredToken_shouldReject() {
        // given - No token saved for this member
        String someToken = jwtTokenProvider.createRefreshToken(MEMBER_ID);

        // when
        String newToken = jwtTokenProvider.createRefreshToken(MEMBER_ID);
        boolean result = refreshTokenService.validateAndRotate(MEMBER_ID, someToken, newToken);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("deleteRefreshToken - should remove token from Redis")
    void deleteRefreshToken_shouldRemoveToken() {
        // given
        String token = jwtTokenProvider.createRefreshToken(MEMBER_ID);
        refreshTokenService.saveRefreshToken(MEMBER_ID, token);

        // when
        refreshTokenService.deleteRefreshToken(MEMBER_ID);

        // then - Token should no longer be valid
        String newToken = jwtTokenProvider.createRefreshToken(MEMBER_ID);
        boolean result = refreshTokenService.validateAndRotate(MEMBER_ID, token, newToken);
        assertThat(result).isFalse();
    }
}
