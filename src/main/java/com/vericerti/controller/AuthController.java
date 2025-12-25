package com.vericerti.controller;

import com.vericerti.application.command.LoginCommand;
import com.vericerti.application.command.SignupCommand;
import com.vericerti.application.dto.SignupResult;
import com.vericerti.application.dto.TokenResult;
import com.vericerti.controller.auth.request.LoginRequest;
import com.vericerti.controller.auth.request.RefreshRequest;
import com.vericerti.controller.auth.request.SignupRequest;
import com.vericerti.controller.auth.response.SignupResponse;
import com.vericerti.controller.auth.response.TokenResponse;
import com.vericerti.domain.auth.service.AuthService;
import com.vericerti.domain.member.entity.Member;
import com.vericerti.domain.member.entity.MemberRole;
import com.vericerti.domain.member.service.MemberService;
import com.vericerti.infrastructure.config.JwtProperties;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final MemberService memberService;
    private final JwtProperties jwtProperties;

    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
        MemberRole role = request.role() != null ? request.role() : MemberRole.DONOR;
        SignupResult result = authService.signup(
                new SignupCommand(request.email(), request.password(), role)
        );
        return ResponseEntity.ok(new SignupResponse(result.memberId(), result.email()));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        TokenResult tokens = authService.login(
                new LoginCommand(request.email(), request.password())
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, createRefreshTokenCookie(tokens.refreshToken()).toString())
                .body(new TokenResponse(tokens.accessToken(), null));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(
            @CookieValue(name = "refreshToken", required = false) String refreshTokenFromCookie,
            @RequestBody(required = false) RefreshRequest request) {

        String refreshToken = refreshTokenFromCookie;
        if (refreshToken == null && request != null) {
            refreshToken = request.refreshToken();
        }

        if (refreshToken == null) {
            return ResponseEntity.badRequest().build();
        }

        TokenResult tokens = authService.refresh(refreshToken);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, createRefreshTokenCookie(tokens.refreshToken()).toString())
                .body(new TokenResponse(tokens.accessToken(), null));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            // UserDetails의 username은 email
            Member member = memberService.findByEmail(userDetails.getUsername());
            authService.logout(member.getId());
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteRefreshTokenCookie().toString())
                .build();
    }

    /**
     * Refresh Token 쿠키 생성
     */
    private ResponseCookie createRefreshTokenCookie(String token) {
        JwtProperties.Cookie cookieConfig = jwtProperties.getCookie();
        long maxAgeSeconds = jwtProperties.getRefreshTokenExpiry() / 1000;

        return ResponseCookie.from(cookieConfig.getName(), token)
                .httpOnly(cookieConfig.isHttpOnly())
                .secure(cookieConfig.isSecure())
                .sameSite(cookieConfig.getSameSite())
                .path(cookieConfig.getPath())
                .maxAge(Duration.ofSeconds(maxAgeSeconds))
                .build();
    }

    /**
     * Refresh Token 쿠키 삭제용 (maxAge=0)
     */
    private ResponseCookie deleteRefreshTokenCookie() {
        JwtProperties.Cookie cookieConfig = jwtProperties.getCookie();

        return ResponseCookie.from(cookieConfig.getName(), "")
                .httpOnly(cookieConfig.isHttpOnly())
                .secure(cookieConfig.isSecure())
                .sameSite(cookieConfig.getSameSite())
                .path(cookieConfig.getPath())
                .maxAge(0)
                .build();
    }
}



