package com.vericerti.controller;

import com.vericerti.config.BaseIntegrationTest;
import com.vericerti.controller.auth.request.LoginRequest;
import com.vericerti.controller.auth.request.SignupRequest;
import com.vericerti.controller.auth.response.SignupResponse;
import com.vericerti.controller.auth.response.TokenResponse;
import com.vericerti.domain.member.entity.MemberRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class AuthControllerIntegrationTest extends BaseIntegrationTest {

    @LocalServerPort
    private int port;

    private final RestTemplate restTemplate = new RestTemplate();

    private String baseUrl() {
        return "http://localhost:" + port + "/api/auth";
    }

    @Test
    @DisplayName("POST /api/auth/signup - 회원가입 성공")
    void signup_shouldReturnMemberId() {
        // given
        SignupRequest request = new SignupRequest(
                "test@example.com",
                "password123",
                MemberRole.DONOR
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<SignupRequest> entity = new HttpEntity<>(request, headers);

        // when
        ResponseEntity<SignupResponse> response = restTemplate.postForEntity(
                baseUrl() + "/signup",
                entity,
                SignupResponse.class
        );

        // then
        assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                () -> assertThat(response.getBody()).isNotNull(),
                () -> assertThat(response.getBody().memberId()).isNotNull(),
                () -> assertThat(response.getBody().email()).isEqualTo("test@example.com")
        );
    }

    @Test
    @DisplayName("POST /api/auth/login - 로그인 성공 및 accessToken 반환")
    void login_shouldReturnAccessToken() {
        // given - 회원가입
        String email = "login@example.com";
        String password = "password123";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        restTemplate.postForEntity(
                baseUrl() + "/signup",
                new HttpEntity<>(new SignupRequest(email, password, MemberRole.DONOR), headers),
                SignupResponse.class
        );

        // when
        ResponseEntity<TokenResponse> response = restTemplate.postForEntity(
                baseUrl() + "/login",
                new HttpEntity<>(new LoginRequest(email, password), headers),
                TokenResponse.class
        );

        // then
        assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                () -> assertThat(response.getBody()).isNotNull(),
                () -> assertThat(response.getBody().accessToken()).isNotBlank(),
                () -> assertThat(response.getHeaders().get("Set-Cookie")).isNotNull()
        );
    }

    @Test
    @DisplayName("POST /api/auth/login - 잘못된 비밀번호")
    void login_withWrongPassword_shouldReturn401() {
        // given
        String email = "wrongpw@example.com";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        restTemplate.postForEntity(
                baseUrl() + "/signup",
                new HttpEntity<>(new SignupRequest(email, "correctPassword", MemberRole.DONOR), headers),
                SignupResponse.class
        );

        // when & then
        try {
            restTemplate.postForEntity(
                    baseUrl() + "/login",
                    new HttpEntity<>(new LoginRequest(email, "wrongPassword"), headers),
                    String.class
            );
            assertThat(false).as("Expected HttpClientErrorException to be thrown").isTrue();
        } catch (HttpClientErrorException e) {
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }
    }

    @Test
    @DisplayName("POST /api/auth/refresh - 토큰 갱신")
    void refresh_shouldReturnNewTokens() {
        // given - 회원가입 + 로그인
        String email = "refresh@example.com";
        String password = "password123";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        restTemplate.postForEntity(
                baseUrl() + "/signup",
                new HttpEntity<>(new SignupRequest(email, password, MemberRole.DONOR), headers),
                SignupResponse.class
        );

        ResponseEntity<TokenResponse> loginResponse = restTemplate.postForEntity(
                baseUrl() + "/login",
                new HttpEntity<>(new LoginRequest(email, password), headers),
                TokenResponse.class
        );

        String setCookieHeader = loginResponse.getHeaders().getFirst("Set-Cookie");
        String cookieValue = setCookieHeader.split(";")[0];

        // when
        HttpHeaders refreshHeaders = new HttpHeaders();
        refreshHeaders.add("Cookie", cookieValue);
        refreshHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<>("", refreshHeaders);

        ResponseEntity<TokenResponse> response = restTemplate.exchange(
                baseUrl() + "/refresh",
                HttpMethod.POST,
                requestEntity,
                TokenResponse.class
        );

        // then
        assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                () -> assertThat(response.getBody()).isNotNull(),
                () -> assertThat(response.getBody().accessToken()).isNotBlank()
        );
    }
}




