package com.vericerti.domain.donation.entity;

import com.vericerti.domain.common.vo.Money;
import com.vericerti.domain.exception.DonationOperationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Donation Entity")
class DonationTest {

    private Donation confirmedDonation;

    @BeforeEach
    void setUp() {
        confirmedDonation = Donation.builder()
                .id(1L)
                .organizationId(100L)
                .memberId(200L)
                .amount(Money.of(new BigDecimal("10000")))
                .purpose("Test donation")
                .status(DonationStatus.CONFIRMED)
                .build();
    }

    @Nested
    @DisplayName("Cancel Operation")
    class CancelOperation {

        @Test
        @DisplayName("should cancel a confirmed donation")
        void shouldCancelConfirmedDonation() {
            // when
            confirmedDonation.cancel("User requested cancellation");

            // then
            assertThat(confirmedDonation.getStatus()).isEqualTo(DonationStatus.CANCELLED);
            assertThat(confirmedDonation.getCancelReason()).isEqualTo("User requested cancellation");
            assertThat(confirmedDonation.getCancelledAt()).isNotNull();
        }

        @Test
        @DisplayName("should throw exception when cancelling already cancelled donation")
        void shouldThrowExceptionWhenCancellingAlreadyCancelled() {
            // given
            confirmedDonation.cancel("First cancellation");

            // when & then
            assertThatThrownBy(() -> confirmedDonation.cancel("Second cancellation"))
                    .isInstanceOf(DonationOperationException.class)
                    .hasMessageContaining("already cancelled");
        }

        @Test
        @DisplayName("should throw exception when cancelling refunded donation")
        void shouldThrowExceptionWhenCancellingRefundedDonation() {
            // given
            confirmedDonation.requestRefund();
            confirmedDonation.completeRefund();

            // when & then
            assertThatThrownBy(() -> confirmedDonation.cancel("Cannot cancel"))
                    .isInstanceOf(DonationOperationException.class)
                    .hasMessageContaining("refunded");
        }
    }

    @Nested
    @DisplayName("Refund Operation")
    class RefundOperation {

        @Test
        @DisplayName("should request refund for confirmed donation")
        void shouldRequestRefundForConfirmedDonation() {
            // when
            confirmedDonation.requestRefund();

            // then
            assertThat(confirmedDonation.getStatus()).isEqualTo(DonationStatus.REFUND_REQUESTED);
            assertThat(confirmedDonation.getRefundRequestedAt()).isNotNull();
        }

        @Test
        @DisplayName("should complete refund after request")
        void shouldCompleteRefundAfterRequest() {
            // given
            confirmedDonation.requestRefund();

            // when
            confirmedDonation.completeRefund();

            // then
            assertThat(confirmedDonation.getStatus()).isEqualTo(DonationStatus.REFUNDED);
        }

        @Test
        @DisplayName("should throw exception when requesting refund for cancelled donation")
        void shouldThrowExceptionWhenRefundingCancelledDonation() {
            // given
            confirmedDonation.cancel("Cancelled");

            // when & then
            assertThatThrownBy(() -> confirmedDonation.requestRefund())
                    .isInstanceOf(DonationOperationException.class)
                    .hasMessageContaining("cancelled");
        }

        @Test
        @DisplayName("should throw exception when completing refund without request")
        void shouldThrowExceptionWhenCompletingRefundWithoutRequest() {
            // when & then
            assertThatThrownBy(() -> confirmedDonation.completeRefund())
                    .isInstanceOf(DonationOperationException.class)
                    .hasMessageContaining("not requested");
        }

        @Test
        @DisplayName("should throw exception when requesting refund twice")
        void shouldThrowExceptionWhenRequestingRefundTwice() {
            // given
            confirmedDonation.requestRefund();

            // when & then
            assertThatThrownBy(() -> confirmedDonation.requestRefund())
                    .isInstanceOf(DonationOperationException.class)
                    .hasMessageContaining("already requested");
        }
    }

    @Nested
    @DisplayName("Status Checks")
    class StatusChecks {

        @Test
        @DisplayName("confirmed donation should be cancellable")
        void confirmedDonationShouldBeCancellable() {
            assertThat(confirmedDonation.isCancellable()).isTrue();
        }

        @Test
        @DisplayName("confirmed donation should be refundable")
        void confirmedDonationShouldBeRefundable() {
            assertThat(confirmedDonation.isRefundable()).isTrue();
        }

        @Test
        @DisplayName("cancelled donation should not be refundable")
        void cancelledDonationShouldNotBeRefundable() {
            // given
            confirmedDonation.cancel("Cancelled");

            // then
            assertThat(confirmedDonation.isRefundable()).isFalse();
        }
    }
}
