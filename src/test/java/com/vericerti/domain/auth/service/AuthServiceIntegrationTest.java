package com.vericerti.domain.auth.service;

import com.vericerti.application.dto.SignupResult;
import com.vericerti.application.dto.TokenResult;
import com.vericerti.config.BaseIntegrationTest;
import com.vericerti.domain.member.entity.MemberRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private AuthService authService;

    @Test
    @DisplayName("signup - 회원가입 성공")
    void signup_shouldCreateMember() {
        // given
        String email = "test@example.com";
        String password = "password123";
        MemberRole role = MemberRole.DONOR;

        // when
        SignupResult response = authService.signup(email, password, role);

        // then
        assertThat(response.memberId()).isNotNull();
        assertThat(response.email()).isEqualTo(email);
        assertThat(memberRepository.existsByEmail(email)).isTrue();
    }

    @Test
    @DisplayName("signup - 중복 이메일 예외")
    void signup_withDuplicateEmail_shouldThrow() {
        // given
        String email = "duplicate@example.com";
        authService.signup(email, "password123", MemberRole.DONOR);

        // when & then
        assertThatThrownBy(() -> authService.signup(email, "password456", MemberRole.DONOR))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email already exists");
    }

    @Test
    @DisplayName("login - 로그인 성공 및 토큰 발급")
    void login_shouldReturnTokens() {
        // given
        String email = "login@example.com";
        String password = "password123";
        authService.signup(email, password, MemberRole.DONOR);

        // when
        TokenResult tokens = authService.login(email, password);

        // then
        assertThat(tokens.accessToken()).isNotBlank();
        assertThat(tokens.refreshToken()).isNotBlank();
    }

    @Test
    @DisplayName("login - 잘못된 비밀번호")
    void login_withWrongPassword_shouldThrow() {
        // given
        String email = "wrong@example.com";
        authService.signup(email, "correctPassword", MemberRole.DONOR);

        // when & then
        assertThatThrownBy(() -> authService.login(email, "wrongPassword"))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("refresh - 토큰 갱신 (RTR)")
    void refresh_shouldRotateTokens() {
        // given
        String email = "refresh@example.com";
        authService.signup(email, "password123", MemberRole.DONOR);
        TokenResult initialTokens = authService.login(email, "password123");

        // when - refresh token은 항상 새로운 값으로 발급됨
        TokenResult newTokens = authService.refresh(initialTokens.refreshToken());

        // then
        assertThat(newTokens.accessToken()).isNotBlank();
        assertThat(newTokens.refreshToken()).isNotBlank();
        // Refresh token은 RTR로 인해 항상 새로운 값
        assertThat(newTokens.refreshToken()).isNotEqualTo(initialTokens.refreshToken());
    }

    @Test
    @DisplayName("refresh - 재사용된 토큰 거부")
    void refresh_withReusedToken_shouldThrow() {
        // given
        String email = "reuse@example.com";
        authService.signup(email, "password123", MemberRole.DONOR);
        TokenResult initialTokens = authService.login(email, "password123");
        String usedToken = initialTokens.refreshToken();

        // 첫 번째 갱신 성공 - 새 토큰으로 교체됨 (RTR)
        authService.refresh(usedToken);

        // when & then - 같은(이미 사용된) 토큰 재사용 시도
        assertThatThrownBy(() -> authService.refresh(usedToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Token reuse detected");
    }

    @Test
    @DisplayName("refresh - 잘못된 토큰")
    void refresh_withInvalidToken_shouldThrow() {
        // when & then
        assertThatThrownBy(() -> authService.refresh("invalid.token.here"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid refresh token");
    }
}

