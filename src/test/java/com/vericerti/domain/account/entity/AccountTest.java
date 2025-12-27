package com.vericerti.domain.account.entity;

import com.vericerti.domain.common.vo.Money;
import com.vericerti.domain.exception.AccountOperationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Account Entity")
class AccountTest {

    private Account activeAccount;

    @BeforeEach
    void setUp() {
        activeAccount = Account.builder()
                .id(1L)
                .organizationId(100L)
                .accountNumber("123-456-789")
                .bankName("Test Bank")
                .accountType(AccountType.OPERATING)
                .accountHolder("Test Holder")
                .balance(new BigDecimal("100000"))
                .status(AccountStatus.ACTIVE)
                .build();
    }

    @Nested
    @DisplayName("Deposit Operation")
    class DepositOperation {

        @Test
        @DisplayName("should deposit money to active account")
        void shouldDepositMoneyToActiveAccount() {
            // when
            activeAccount.deposit(Money.of(new BigDecimal("50000")));

            // then
            assertThat(activeAccount.getBalance()).isEqualByComparingTo(new BigDecimal("150000"));
        }

        @Test
        @DisplayName("should throw exception when depositing to suspended account")
        void shouldThrowExceptionWhenDepositingToSuspendedAccount() {
            // given
            activeAccount.suspend("Investigation");

            // when & then
            assertThatThrownBy(() -> activeAccount.deposit(Money.of(new BigDecimal("10000"))))
                    .isInstanceOf(AccountOperationException.class)
                    .hasMessageContaining("suspended");
        }

        @Test
        @DisplayName("should throw exception when depositing to closed account")
        void shouldThrowExceptionWhenDepositingToClosedAccount() {
            // given
            activeAccount.close();

            // when & then
            assertThatThrownBy(() -> activeAccount.deposit(Money.of(new BigDecimal("10000"))))
                    .isInstanceOf(AccountOperationException.class)
                    .hasMessageContaining("closed");
        }
    }

    @Nested
    @DisplayName("Withdraw Operation")
    class WithdrawOperation {

        @Test
        @DisplayName("should withdraw money from active account")
        void shouldWithdrawMoneyFromActiveAccount() {
            // when
            activeAccount.withdraw(Money.of(new BigDecimal("30000")));

            // then
            assertThat(activeAccount.getBalance()).isEqualByComparingTo(new BigDecimal("70000"));
        }

        @Test
        @DisplayName("should throw exception when withdrawing more than balance")
        void shouldThrowExceptionWhenWithdrawingMoreThanBalance() {
            // when & then
            assertThatThrownBy(() -> activeAccount.withdraw(Money.of(new BigDecimal("200000"))))
                    .isInstanceOf(AccountOperationException.class)
                    .hasMessageContaining("Insufficient balance");
        }

        @Test
        @DisplayName("should throw exception when withdrawing from suspended account")
        void shouldThrowExceptionWhenWithdrawingFromSuspendedAccount() {
            // given
            activeAccount.suspend("Fraud investigation");

            // when & then
            assertThatThrownBy(() -> activeAccount.withdraw(Money.of(new BigDecimal("10000"))))
                    .isInstanceOf(AccountOperationException.class)
                    .hasMessageContaining("suspended");
        }
    }

    @Nested
    @DisplayName("Suspend Operation")
    class SuspendOperation {

        @Test
        @DisplayName("should suspend an active account")
        void shouldSuspendActiveAccount() {
            // when
            activeAccount.suspend("Policy violation");

            // then
            assertThat(activeAccount.getStatus()).isEqualTo(AccountStatus.SUSPENDED);
            assertThat(activeAccount.getSuspendedAt()).isNotNull();
            assertThat(activeAccount.getSuspensionReason()).isEqualTo("Policy violation");
        }

        @Test
        @DisplayName("should throw exception when suspending already suspended account")
        void shouldThrowExceptionWhenSuspendingAlreadySuspended() {
            // given
            activeAccount.suspend("First suspension");

            // when & then
            assertThatThrownBy(() -> activeAccount.suspend("Second suspension"))
                    .isInstanceOf(AccountOperationException.class)
                    .hasMessageContaining("already suspended");
        }
    }

    @Nested
    @DisplayName("Activate Operation")
    class ActivateOperation {

        @Test
        @DisplayName("should activate a suspended account")
        void shouldActivateSuspendedAccount() {
            // given
            activeAccount.suspend("Temporary suspension");

            // when
            activeAccount.activate();

            // then
            assertThat(activeAccount.getStatus()).isEqualTo(AccountStatus.ACTIVE);
            assertThat(activeAccount.getSuspendedAt()).isNull();
            assertThat(activeAccount.getSuspensionReason()).isNull();
        }

        @Test
        @DisplayName("should throw exception when activating already active account")
        void shouldThrowExceptionWhenActivatingAlreadyActive() {
            // when & then
            assertThatThrownBy(() -> activeAccount.activate())
                    .isInstanceOf(AccountOperationException.class)
                    .hasMessageContaining("already active");
        }

        @Test
        @DisplayName("should throw exception when activating closed account")
        void shouldThrowExceptionWhenActivatingClosedAccount() {
            // given
            activeAccount.close();

            // when & then
            assertThatThrownBy(() -> activeAccount.activate())
                    .isInstanceOf(AccountOperationException.class)
                    .hasMessageContaining("closed");
        }
    }

    @Nested
    @DisplayName("Close Operation")
    class CloseOperation {

        @Test
        @DisplayName("should close an active account")
        void shouldCloseActiveAccount() {
            // when
            activeAccount.close();

            // then
            assertThat(activeAccount.getStatus()).isEqualTo(AccountStatus.CLOSED);
            assertThat(activeAccount.getClosedAt()).isNotNull();
        }

        @Test
        @DisplayName("should throw exception when closing already closed account")
        void shouldThrowExceptionWhenClosingAlreadyClosed() {
            // given
            activeAccount.close();

            // when & then
            assertThatThrownBy(() -> activeAccount.close())
                    .isInstanceOf(AccountOperationException.class)
                    .hasMessageContaining("closed");
        }
    }

    @Nested
    @DisplayName("Status Checks")
    class StatusChecks {

        @Test
        @DisplayName("active account should return isActive true")
        void activeAccountShouldReturnIsActiveTrue() {
            assertThat(activeAccount.isActive()).isTrue();
            assertThat(activeAccount.isSuspended()).isFalse();
            assertThat(activeAccount.isClosed()).isFalse();
        }

        @Test
        @DisplayName("suspended account should return isSuspended true")
        void suspendedAccountShouldReturnIsSuspendedTrue() {
            // given
            activeAccount.suspend("Reason");

            // then
            assertThat(activeAccount.isActive()).isFalse();
            assertThat(activeAccount.isSuspended()).isTrue();
        }
    }

    @Nested
    @DisplayName("Sync Balance")
    class SyncBalance {

        @Test
        @DisplayName("should sync balance regardless of account status")
        void shouldSyncBalance() {
            // given
            activeAccount.suspend("Investigation");

            // when - sync should work even on suspended account
            activeAccount.syncBalance(new BigDecimal("500000"));

            // then
            assertThat(activeAccount.getBalance()).isEqualByComparingTo(new BigDecimal("500000"));
        }
    }
}
