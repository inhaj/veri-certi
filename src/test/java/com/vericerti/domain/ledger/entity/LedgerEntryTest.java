package com.vericerti.domain.ledger.entity;

import com.vericerti.domain.common.vo.DataHash;
import com.vericerti.domain.exception.IllegalStateTransitionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("LedgerEntry Entity")
class LedgerEntryTest {

    private static final String VALID_HASH = "a".repeat(64);
    private static final String TX_HASH = "0x" + "b".repeat(64);

    private LedgerEntry pendingEntry;

    @BeforeEach
    void setUp() {
        pendingEntry = LedgerEntry.builder()
                .id(1L)
                .organizationId(100L)
                .entityType(LedgerEntityType.DONATION)
                .entityId(200L)
                .dataHash(DataHash.of(VALID_HASH))
                .status(LedgerStatus.PENDING)
                .build();
    }

    @Nested
    @DisplayName("Mark As Recorded")
    class MarkAsRecorded {

        @Test
        @DisplayName("should mark pending entry as recorded")
        void shouldMarkPendingEntryAsRecorded() {
            // when
            pendingEntry.markAsRecorded(TX_HASH);

            // then
            assertThat(pendingEntry.getStatus()).isEqualTo(LedgerStatus.RECORDED);
            assertThat(pendingEntry.getBlockchainTxHash()).isEqualTo(TX_HASH);
        }

        @Test
        @DisplayName("should throw exception when marking recorded entry as recorded")
        void shouldThrowExceptionWhenMarkingRecordedEntryAsRecorded() {
            // given
            pendingEntry.markAsRecorded(TX_HASH);

            // when & then
            assertThatThrownBy(() -> pendingEntry.markAsRecorded("0x" + "c".repeat(64)))
                    .isInstanceOf(IllegalStateTransitionException.class)
                    .hasMessageContaining("RECORDED")
                    .hasMessageContaining("Cannot transition");
        }

        @Test
        @DisplayName("should throw exception when marking failed entry as recorded")
        void shouldThrowExceptionWhenMarkingFailedEntryAsRecorded() {
            // given
            pendingEntry.markAsFailed();

            // when & then
            assertThatThrownBy(() -> pendingEntry.markAsRecorded(TX_HASH))
                    .isInstanceOf(IllegalStateTransitionException.class)
                    .hasMessageContaining("FAILED");
        }

        @Test
        @DisplayName("should throw exception for null transaction hash")
        void shouldThrowExceptionForNullTxHash() {
            // when & then
            assertThatThrownBy(() -> pendingEntry.markAsRecorded(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("empty");
        }

        @Test
        @DisplayName("should throw exception for blank transaction hash")
        void shouldThrowExceptionForBlankTxHash() {
            // when & then
            assertThatThrownBy(() -> pendingEntry.markAsRecorded("   "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("empty");
        }
    }

    @Nested
    @DisplayName("Mark As Failed")
    class MarkAsFailed {

        @Test
        @DisplayName("should mark pending entry as failed")
        void shouldMarkPendingEntryAsFailed() {
            // when
            pendingEntry.markAsFailed();

            // then
            assertThat(pendingEntry.getStatus()).isEqualTo(LedgerStatus.FAILED);
        }

        @Test
        @DisplayName("should throw exception when marking recorded entry as failed")
        void shouldThrowExceptionWhenMarkingRecordedEntryAsFailed() {
            // given
            pendingEntry.markAsRecorded(TX_HASH);

            // when & then
            assertThatThrownBy(() -> pendingEntry.markAsFailed())
                    .isInstanceOf(IllegalStateTransitionException.class)
                    .hasMessageContaining("RECORDED");
        }
    }

    @Nested
    @DisplayName("Retry Operation")
    class RetryOperation {

        @Test
        @DisplayName("should retry failed entry")
        void shouldRetryFailedEntry() {
            // given
            pendingEntry.markAsFailed();

            // when
            pendingEntry.retry();

            // then
            assertThat(pendingEntry.getStatus()).isEqualTo(LedgerStatus.PENDING);
            assertThat(pendingEntry.getBlockchainTxHash()).isNull();
        }

        @Test
        @DisplayName("should throw exception when retrying pending entry")
        void shouldThrowExceptionWhenRetryingPendingEntry() {
            // when & then
            assertThatThrownBy(() -> pendingEntry.retry())
                    .isInstanceOf(IllegalStateTransitionException.class)
                    .hasMessageContaining("PENDING");
        }

        @Test
        @DisplayName("should throw exception when retrying recorded entry")
        void shouldThrowExceptionWhenRetryingRecordedEntry() {
            // given
            pendingEntry.markAsRecorded(TX_HASH);

            // when & then
            assertThatThrownBy(() -> pendingEntry.retry())
                    .isInstanceOf(IllegalStateTransitionException.class)
                    .hasMessageContaining("RECORDED");
        }
    }

    @Nested
    @DisplayName("Status Checks")
    class StatusChecks {

        @Test
        @DisplayName("pending entry should return isPending true")
        void pendingEntryShouldReturnIsPendingTrue() {
            assertThat(pendingEntry.isPending()).isTrue();
            assertThat(pendingEntry.isRecorded()).isFalse();
        }

        @Test
        @DisplayName("recorded entry should return isRecorded true")
        void recordedEntryShouldReturnIsRecordedTrue() {
            // given
            pendingEntry.markAsRecorded(TX_HASH);

            // then
            assertThat(pendingEntry.isPending()).isFalse();
            assertThat(pendingEntry.isRecorded()).isTrue();
        }
    }
}
