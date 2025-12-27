package com.vericerti.domain.receipt.entity;

import com.vericerti.domain.common.vo.BusinessNumber;
import com.vericerti.domain.common.vo.Money;
import com.vericerti.domain.exception.ReceiptOperationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Receipt Entity")
class ReceiptTest {

    private Receipt pendingReceipt;

    @BeforeEach
    void setUp() {
        pendingReceipt = Receipt.builder()
                .id(1L)
                .organizationId(100L)
                .amount(Money.of(new BigDecimal("50000")))
                .issueDate(LocalDate.now())
                .merchantName("Test Store")
                .merchantBusinessNumber(BusinessNumber.of("123-45-67890"))
                .category(ReceiptCategory.SUPPLIES)
                .status(ReceiptStatus.PENDING)
                .build();
    }

    @Nested
    @DisplayName("Verify Operation")
    class VerifyOperation {

        @Test
        @DisplayName("should verify a pending receipt")
        void shouldVerifyPendingReceipt() {
            // when
            pendingReceipt.verify();

            // then
            assertThat(pendingReceipt.getStatus()).isEqualTo(ReceiptStatus.VERIFIED);
            assertThat(pendingReceipt.getVerifiedAt()).isNotNull();
        }

        @Test
        @DisplayName("should throw exception when verifying already verified receipt")
        void shouldThrowExceptionWhenVerifyingAlreadyVerified() {
            // given
            pendingReceipt.verify();

            // when & then
            assertThatThrownBy(() -> pendingReceipt.verify())
                    .isInstanceOf(ReceiptOperationException.class)
                    .hasMessageContaining("already verified");
        }

        @Test
        @DisplayName("should throw exception when verifying rejected receipt")
        void shouldThrowExceptionWhenVerifyingRejected() {
            // given
            pendingReceipt.reject("Invalid");

            // when & then
            assertThatThrownBy(() -> pendingReceipt.verify())
                    .isInstanceOf(ReceiptOperationException.class)
                    .hasMessageContaining("already rejected");
        }
    }

    @Nested
    @DisplayName("Reject Operation")
    class RejectOperation {

        @Test
        @DisplayName("should reject a pending receipt")
        void shouldRejectPendingReceipt() {
            // when
            pendingReceipt.reject("Invalid receipt image");

            // then
            assertThat(pendingReceipt.getStatus()).isEqualTo(ReceiptStatus.REJECTED);
            assertThat(pendingReceipt.getRejectedAt()).isNotNull();
            assertThat(pendingReceipt.getRejectionReason()).isEqualTo("Invalid receipt image");
        }

        @Test
        @DisplayName("should throw exception when rejecting already verified receipt")
        void shouldThrowExceptionWhenRejectingVerified() {
            // given
            pendingReceipt.verify();

            // when & then
            assertThatThrownBy(() -> pendingReceipt.reject("Reason"))
                    .isInstanceOf(ReceiptOperationException.class)
                    .hasMessageContaining("already verified");
        }
    }

    @Nested
    @DisplayName("Archive Operation")
    class ArchiveOperation {

        @Test
        @DisplayName("should archive a verified receipt")
        void shouldArchiveVerifiedReceipt() {
            // given
            pendingReceipt.verify();

            // when
            pendingReceipt.archive();

            // then
            assertThat(pendingReceipt.getStatus()).isEqualTo(ReceiptStatus.ARCHIVED);
            assertThat(pendingReceipt.getArchivedAt()).isNotNull();
        }

        @Test
        @DisplayName("should archive a rejected receipt")
        void shouldArchiveRejectedReceipt() {
            // given
            pendingReceipt.reject("Reason");

            // when
            pendingReceipt.archive();

            // then
            assertThat(pendingReceipt.getStatus()).isEqualTo(ReceiptStatus.ARCHIVED);
        }

        @Test
        @DisplayName("should throw exception when archiving pending receipt")
        void shouldThrowExceptionWhenArchivingPending() {
            // when & then
            assertThatThrownBy(() -> pendingReceipt.archive())
                    .isInstanceOf(ReceiptOperationException.class)
                    .hasMessageContaining("pending");
        }
    }

    @Nested
    @DisplayName("Reopen Operation")
    class ReopenOperation {

        @Test
        @DisplayName("should reopen a rejected receipt")
        void shouldReopenRejectedReceipt() {
            // given
            pendingReceipt.reject("Reason");

            // when
            pendingReceipt.reopenForReview();

            // then
            assertThat(pendingReceipt.getStatus()).isEqualTo(ReceiptStatus.PENDING);
            assertThat(pendingReceipt.getRejectedAt()).isNull();
            assertThat(pendingReceipt.getRejectionReason()).isNull();
        }

        @Test
        @DisplayName("should throw exception when reopening pending receipt")
        void shouldThrowExceptionWhenReopeningPending() {
            // when & then
            assertThatThrownBy(() -> pendingReceipt.reopenForReview())
                    .isInstanceOf(ReceiptOperationException.class)
                    .hasMessageContaining("rejected");
        }
    }

    @Nested
    @DisplayName("Status Checks")
    class StatusChecks {

        @Test
        @DisplayName("pending receipt should return isPending true")
        void pendingReceiptShouldReturnIsPendingTrue() {
            assertThat(pendingReceipt.isPending()).isTrue();
            assertThat(pendingReceipt.isVerified()).isFalse();
            assertThat(pendingReceipt.isModifiable()).isTrue();
        }

        @Test
        @DisplayName("verified receipt should not be modifiable")
        void verifiedReceiptShouldNotBeModifiable() {
            // given
            pendingReceipt.verify();

            // then
            assertThat(pendingReceipt.isPending()).isFalse();
            assertThat(pendingReceipt.isVerified()).isTrue();
            assertThat(pendingReceipt.isModifiable()).isFalse();
        }
    }
}
