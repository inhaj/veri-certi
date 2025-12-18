package com.vericerti.domain.auth.service;

import com.vericerti.application.dto.SignupResult;
import com.vericerti.application.dto.TokenResult;
import com.vericerti.domain.member.entity.Member;
import com.vericerti.domain.member.entity.MemberRole;
import com.vericerti.domain.member.repository.MemberRepository;
import com.vericerti.infrastructure.security.JwtTokenProvider;
import com.vericerti.infrastructure.security.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public SignupResult signup(String email, String password, MemberRole role) {
        if (memberRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists: " + email);
        }

        Member member = Member.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(role)
                .build();

        memberRepository.save(member);

        return new SignupResult(member.getId(), member.getEmail());
    }

    @Transactional
    public TokenResult login(String email, String password) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String accessToken = jwtTokenProvider.createAccessToken(member.getId(), member.getEmail());
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getId());

        refreshTokenService.saveRefreshToken(member.getId(), refreshToken);

        return new TokenResult(accessToken, refreshToken);
    }

    @Transactional
    public TokenResult refresh(String oldRefreshToken) {
        if (!jwtTokenProvider.validateToken(oldRefreshToken)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        String tokenType = jwtTokenProvider.getTokenType(oldRefreshToken);
        if (!"refresh".equals(tokenType)) {
            throw new IllegalArgumentException("Not a refresh token");
        }

        Long memberId = jwtTokenProvider.getMemberIdFromToken(oldRefreshToken);
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String newAccessToken = jwtTokenProvider.createAccessToken(member.getId(), member.getEmail());
        String newRefreshToken = jwtTokenProvider.createRefreshToken(member.getId());

        boolean rotated = refreshTokenService.validateAndRotate(memberId, oldRefreshToken, newRefreshToken);
        if (!rotated) {
            throw new IllegalArgumentException("Token reuse detected. Session invalidated.");
        }

        return new TokenResult(newAccessToken, newRefreshToken);
    }

    public void logout(Long memberId) {
        refreshTokenService.deleteRefreshToken(memberId);
    }
}

