package com.vericerti.domain.member.entity;

import com.vericerti.domain.common.vo.Email;
import com.vericerti.domain.exception.MemberOperationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Member Entity")
class MemberTest {

    private Member activeMember;

    @BeforeEach
    void setUp() {
        activeMember = Member.builder()
                .id(1L)
                .email(Email.of("test@example.com"))
                .password("encodedPassword")
                .role(MemberRole.DONOR)
                .status(MemberStatus.ACTIVE)
                .build();
    }

    @Nested
    @DisplayName("Suspend Operation")
    class SuspendOperation {

        @Test
        @DisplayName("should suspend active member")
        void shouldSuspendActiveMember() {
            // when
            activeMember.suspend("Policy violation");

            // then
            assertThat(activeMember.getStatus()).isEqualTo(MemberStatus.SUSPENDED);
            assertThat(activeMember.getSuspendedAt()).isNotNull();
            assertThat(activeMember.getSuspensionReason()).isEqualTo("Policy violation");
        }

        @Test
        @DisplayName("should throw exception when suspending already suspended member")
        void shouldThrowExceptionWhenSuspendingAlreadySuspended() {
            // given
            activeMember.suspend("First suspension");

            // when & then
            assertThatThrownBy(() -> activeMember.suspend("Second suspension"))
                    .isInstanceOf(MemberOperationException.class)
                    .hasMessageContaining("already suspended");
        }

        @Test
        @DisplayName("should throw exception when suspending withdrawn member")
        void shouldThrowExceptionWhenSuspendingWithdrawn() {
            // given
            activeMember.withdraw();

            // when & then
            assertThatThrownBy(() -> activeMember.suspend("Reason"))
                    .isInstanceOf(MemberOperationException.class)
                    .hasMessageContaining("withdrawn");
        }
    }

    @Nested
    @DisplayName("Reactivate Operation")
    class ReactivateOperation {

        @Test
        @DisplayName("should reactivate suspended member")
        void shouldReactivateSuspendedMember() {
            // given
            activeMember.suspend("Temporary");

            // when
            activeMember.reactivate();

            // then
            assertThat(activeMember.getStatus()).isEqualTo(MemberStatus.ACTIVE);
            assertThat(activeMember.getSuspendedAt()).isNull();
            assertThat(activeMember.getSuspensionReason()).isNull();
        }

        @Test
        @DisplayName("should throw exception when reactivating already active member")
        void shouldThrowExceptionWhenReactivatingActive() {
            // when & then
            assertThatThrownBy(() -> activeMember.reactivate())
                    .isInstanceOf(MemberOperationException.class)
                    .hasMessageContaining("already active");
        }

        @Test
        @DisplayName("should throw exception when reactivating withdrawn member")
        void shouldThrowExceptionWhenReactivatingWithdrawn() {
            // given
            activeMember.withdraw();

            // when & then
            assertThatThrownBy(() -> activeMember.reactivate())
                    .isInstanceOf(MemberOperationException.class)
                    .hasMessageContaining("withdrawn");
        }
    }

    @Nested
    @DisplayName("Withdraw Operation")
    class WithdrawOperation {

        @Test
        @DisplayName("should withdraw active member")
        void shouldWithdrawActiveMember() {
            // when
            activeMember.withdraw();

            // then
            assertThat(activeMember.getStatus()).isEqualTo(MemberStatus.WITHDRAWN);
            assertThat(activeMember.getWithdrawnAt()).isNotNull();
        }

        @Test
        @DisplayName("should withdraw suspended member")
        void shouldWithdrawSuspendedMember() {
            // given
            activeMember.suspend("Reason");

            // when
            activeMember.withdraw();

            // then
            assertThat(activeMember.getStatus()).isEqualTo(MemberStatus.WITHDRAWN);
        }

        @Test
        @DisplayName("should throw exception when withdrawing already withdrawn member")
        void shouldThrowExceptionWhenWithdrawingAlreadyWithdrawn() {
            // given
            activeMember.withdraw();

            // when & then
            assertThatThrownBy(() -> activeMember.withdraw())
                    .isInstanceOf(MemberOperationException.class)
                    .hasMessageContaining("already withdrawn");
        }
    }

    @Nested
    @DisplayName("Status Checks")
    class StatusChecks {

        @Test
        @DisplayName("active member should return isActive true")
        void activeMemberShouldReturnIsActiveTrue() {
            assertThat(activeMember.isActive()).isTrue();
            assertThat(activeMember.isSuspended()).isFalse();
            assertThat(activeMember.isWithdrawn()).isFalse();
        }

        @Test
        @DisplayName("suspended member should return isSuspended true")
        void suspendedMemberShouldReturnIsSuspendedTrue() {
            // given
            activeMember.suspend("Reason");

            // then
            assertThat(activeMember.isActive()).isFalse();
            assertThat(activeMember.isSuspended()).isTrue();
        }

        @Test
        @DisplayName("withdrawn member should return isWithdrawn true")
        void withdrawnMemberShouldReturnIsWithdrawnTrue() {
            // given
            activeMember.withdraw();

            // then
            assertThat(activeMember.isActive()).isFalse();
            assertThat(activeMember.isWithdrawn()).isTrue();
        }
    }
}
