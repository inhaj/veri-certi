package com.vericerti.domain.member.service;

import com.vericerti.application.command.SignupCommand;
import com.vericerti.config.BaseIntegrationTest;
import com.vericerti.domain.auth.service.AuthService;
import com.vericerti.domain.member.entity.Member;
import com.vericerti.domain.member.entity.MemberRole;
import com.vericerti.infrastructure.exception.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class MemberServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private AuthService authService;

    private static final String TEST_EMAIL = "member@example.com";
    private static final String TEST_PASSWORD = "password123";

    private Long createdMemberId;

    @BeforeEach
    void setUp() {
        // 테스트 멤버 생성
        createdMemberId = authService.signup(
                new SignupCommand(TEST_EMAIL, TEST_PASSWORD, MemberRole.DONOR)
        ).memberId();
    }

    @Test
    @DisplayName("findById - ID로 멤버 조회")
    void findById_shouldReturnMember() {
        // when
        Member found = memberService.findById(createdMemberId);

        // then
        assertAll(
                () -> assertThat(found.getId()).isEqualTo(createdMemberId),
                () -> assertThat(found.getEmailValue().orElse("")).isEqualTo(TEST_EMAIL),
                () -> assertThat(found.getRole()).isEqualTo(MemberRole.DONOR)
        );
    }

    @Test
    @DisplayName("findById - 존재하지 않는 ID")
    void findById_withInvalidId_shouldThrow() {
        assertThatThrownBy(() -> memberService.findById(999L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("findByEmail - 이메일로 멤버 조회")
    void findByEmail_shouldReturnMember() {
        // when
        Member found = memberService.findByEmail(TEST_EMAIL);

        // then
        assertAll(
                () -> assertThat(found.getId()).isEqualTo(createdMemberId),
                () -> assertThat(found.getEmailValue().orElse("")).isEqualTo(TEST_EMAIL)
        );
    }

    @Test
    @DisplayName("findByEmail - 존재하지 않는 이메일")
    void findByEmail_withInvalidEmail_shouldThrow() {
        assertThatThrownBy(() -> memberService.findByEmail("nonexistent@example.com"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("existsByEmail - 존재하는 이메일")
    void existsByEmail_withExistingEmail_shouldReturnTrue() {
        // when
        boolean exists = memberService.existsByEmail(TEST_EMAIL);

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsByEmail - 존재하지 않는 이메일")
    void existsByEmail_withNonExistingEmail_shouldReturnFalse() {
        // when
        boolean exists = memberService.existsByEmail("nonexistent@example.com");

        // then
        assertThat(exists).isFalse();
    }
}
