package com.vericerti.domain.auth.service;

import com.vericerti.application.command.LoginCommand;
import com.vericerti.application.command.SignupCommand;
import com.vericerti.application.dto.SignupResult;
import com.vericerti.application.dto.TokenResult;
import com.vericerti.domain.common.vo.Email;
import com.vericerti.domain.member.entity.Member;
import com.vericerti.domain.member.repository.MemberRepository;
import com.vericerti.infrastructure.exception.AuthenticationException;
import com.vericerti.infrastructure.exception.DuplicateException;
import com.vericerti.infrastructure.exception.EntityNotFoundException;
import com.vericerti.infrastructure.security.JwtTokenProvider;
import com.vericerti.infrastructure.security.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public SignupResult signup(SignupCommand command) {
        if (memberRepository.existsByEmail(command.email())) {
            throw DuplicateException.email(command.email());
        }

        Member member = Member.builder()
                .email(Email.of(command.email()))
                .password(passwordEncoder.encode(command.password()))
                .role(command.role())
                .build();

        memberRepository.save(member);
        log.info("event=user_signup email={}", command.email());

        return new SignupResult(member.getId(), member.getEmailValue().orElse(""));
    }

    @Transactional
    public TokenResult login(LoginCommand command) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(command.email(), command.password())
        );

        Member member = memberRepository.findByEmail(command.email())
                .orElseThrow(() -> EntityNotFoundException.user(command.email()));

        String accessToken = jwtTokenProvider.createAccessToken(member.getId(), member.getEmailValue().orElseThrow());
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getId());

        refreshTokenService.saveRefreshToken(member.getId(), refreshToken);
        log.info("event=user_login email={}", command.email());

        return new TokenResult(accessToken, refreshToken);
    }

    @Transactional
    public TokenResult refresh(String oldRefreshToken) {
        if (!jwtTokenProvider.validateToken(oldRefreshToken)) {
            throw AuthenticationException.invalidToken();
        }

        String tokenType = jwtTokenProvider.getTokenType(oldRefreshToken);
        if (!JwtTokenProvider.TOKEN_TYPE_REFRESH.equals(tokenType)) {
            throw AuthenticationException.invalidToken();
        }

        Long memberId = jwtTokenProvider.getMemberIdFromToken(oldRefreshToken);
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> EntityNotFoundException.user(memberId));

        String newAccessToken = jwtTokenProvider.createAccessToken(member.getId(), member.getEmailValue().orElseThrow());
        String newRefreshToken = jwtTokenProvider.createRefreshToken(member.getId());

        boolean rotated = refreshTokenService.validateAndRotate(memberId, oldRefreshToken, newRefreshToken);
        if (!rotated) {
            throw AuthenticationException.tokenReuseDetected();
        }

        log.info("event=token_refresh memberId={}", memberId);
        return new TokenResult(newAccessToken, newRefreshToken);
    }

    public void logout(Long memberId) {
        refreshTokenService.deleteRefreshToken(memberId);
        log.info("event=user_logout memberId={}", memberId);
    }
}


