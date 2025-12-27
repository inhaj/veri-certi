package com.vericerti.application.usecase;

import com.vericerti.application.command.RecordDonationCommand;
import com.vericerti.application.dto.DonationResult;
import com.vericerti.config.BaseIntegrationTest;
import com.vericerti.domain.common.vo.BusinessNumber;
import com.vericerti.domain.donation.entity.Donation;
import com.vericerti.domain.ledger.entity.LedgerEntry;
import com.vericerti.domain.ledger.entity.LedgerEntityType;
import com.vericerti.domain.ledger.entity.LedgerStatus;
import com.vericerti.domain.ledger.repository.LedgerEntryRepository;
import com.vericerti.domain.donation.repository.DonationRepository;
import com.vericerti.domain.organization.entity.Organization;
import com.vericerti.domain.organization.repository.OrganizationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class RecordDonationUseCaseIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private RecordDonationUseCase recordDonationUseCase;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private DonationRepository donationRepository;

    @Autowired
    private LedgerEntryRepository ledgerEntryRepository;

    private Organization testOrg;

    @BeforeEach
    void setUp() {
        testOrg = organizationRepository.save(Organization.builder()
                .name("테스트 단체")
                .businessNumber(BusinessNumber.of("BN-" + UUID.randomUUID()))
                .description("테스트용")
                .build());
    }

    @Test
    @DisplayName("execute - 기부와 LedgerEntry가 함께 생성됨")
    void execute_shouldCreateDonationAndLedgerEntry() {
        // given
        Long memberId = 100L;
        BigDecimal amount = new BigDecimal("100000.00");
        String purpose = "의료 지원";
        byte[] receiptFile = "receipt content".getBytes();
        String filename = "receipt.pdf";

        // when
        DonationResult result = recordDonationUseCase.execute(
                new RecordDonationCommand(testOrg.getId(), memberId, amount, purpose, receiptFile, filename)
        );

        // then
        Donation donation = result.donation();
        LedgerEntry ledgerEntry = result.ledgerEntry();

        assertAll(
                // Donation 검증
                () -> assertThat(donation.getId()).isNotNull(),
                () -> assertThat(donation.getOrganizationId()).isEqualTo(testOrg.getId()),
                () -> assertThat(donation.getMemberId()).isEqualTo(memberId),
                () -> assertThat(donation.getAmountValue().orElse(null)).isEqualByComparingTo(amount),
                () -> assertThat(donation.getPurpose()).isEqualTo(purpose),
                // LedgerEntry 검증
                () -> assertThat(ledgerEntry.getId()).isNotNull(),
                () -> assertThat(ledgerEntry.getOrganizationId()).isEqualTo(testOrg.getId()),
                () -> assertThat(ledgerEntry.getEntityType()).isEqualTo(LedgerEntityType.DONATION),
                () -> assertThat(ledgerEntry.getEntityId()).isEqualTo(donation.getId()),
                () -> assertThat(ledgerEntry.getDataHashValue().orElse("")).hasSize(64),
                () -> assertThat(ledgerEntry.getStatus()).isEqualTo(LedgerStatus.PENDING)
        );
    }

    @Test
    @DisplayName("execute - 트랜잭션으로 묶여있음 (Donation과 LedgerEntry 동시 저장)")
    void execute_shouldBeTransactional() {
        // given
        DonationResult result = recordDonationUseCase.execute(
                new RecordDonationCommand(testOrg.getId(), 100L, new BigDecimal("50000"), "테스트", "content".getBytes(), "test.pdf")
        );

        // when
        boolean donationExists = donationRepository.existsById(result.donation().getId());
        boolean ledgerExists = ledgerEntryRepository.existsById(result.ledgerEntry().getId());

        // then
        assertAll(
                () -> assertThat(donationExists).isTrue(),
                () -> assertThat(ledgerExists).isTrue()
        );
    }
}




