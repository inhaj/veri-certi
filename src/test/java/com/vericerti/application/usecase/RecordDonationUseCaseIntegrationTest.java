package com.vericerti.application.usecase;

import com.vericerti.application.dto.DonationResult;
import com.vericerti.config.BaseIntegrationTest;
import com.vericerti.domain.donation.entity.Donation;
import com.vericerti.domain.ledger.entity.LedgerEntry;
import com.vericerti.domain.ledger.entity.LedgerEntityType;
import com.vericerti.domain.ledger.entity.LedgerStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class RecordDonationUseCaseIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private RecordDonationUseCase recordDonationUseCase;

    @Test
    @DisplayName("execute - 기부와 LedgerEntry가 함께 생성됨")
    void execute_shouldCreateDonationAndLedgerEntry() {
        // given
        Long organizationId = 1L;
        Long memberId = 100L;
        BigDecimal amount = new BigDecimal("100000.00");
        String purpose = "의료 지원";
        byte[] receiptFile = "receipt content".getBytes();
        String filename = "receipt.pdf";

        // when
        DonationResult result = recordDonationUseCase.execute(
                organizationId, memberId, amount, purpose, receiptFile, filename
        );

        // then
        Donation donation = result.donation();
        LedgerEntry ledgerEntry = result.ledgerEntry();

        assertThat(donation.getId()).isNotNull();
        assertThat(donation.getOrganizationId()).isEqualTo(organizationId);
        assertThat(donation.getMemberId()).isEqualTo(memberId);
        assertThat(donation.getAmount()).isEqualByComparingTo(amount);
        assertThat(donation.getPurpose()).isEqualTo(purpose);

        assertThat(ledgerEntry.getId()).isNotNull();
        assertThat(ledgerEntry.getOrganizationId()).isEqualTo(organizationId);
        assertThat(ledgerEntry.getEntityType()).isEqualTo(LedgerEntityType.DONATION);
        assertThat(ledgerEntry.getEntityId()).isEqualTo(donation.getId());
        assertThat(ledgerEntry.getDataHash()).hasSize(64);
        assertThat(ledgerEntry.getStatus()).isEqualTo(LedgerStatus.PENDING);
    }

    @Test
    @DisplayName("execute - 트랜잭션으로 묶여있음 (Donation과 LedgerEntry 동시 저장)")
    void execute_shouldBeTransactional() {
        // given
        DonationResult result = recordDonationUseCase.execute(
                1L, 100L, new BigDecimal("50000"), "테스트", "content".getBytes(), "test.pdf"
        );

        // when
        boolean donationExists = donationRepository.existsById(result.donation().getId());
        boolean ledgerExists = ledgerEntryRepository.existsById(result.ledgerEntry().getId());

        // then
        assertThat(donationExists).isTrue();
        assertThat(ledgerExists).isTrue();
    }
}

