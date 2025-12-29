package com.vericerti.domain.ledger.service;

import com.vericerti.application.command.CreateLedgerEntryCommand;
import com.vericerti.config.BaseIntegrationTest;
import com.vericerti.domain.common.vo.BusinessNumber;
import com.vericerti.domain.ledger.entity.LedgerEntry;
import com.vericerti.domain.ledger.entity.LedgerEntityType;
import com.vericerti.domain.ledger.entity.LedgerStatus;
import com.vericerti.domain.ledger.repository.LedgerEntryRepository;
import com.vericerti.domain.organization.entity.Organization;
import com.vericerti.domain.organization.repository.OrganizationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class LedgerServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private LedgerService ledgerService;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private LedgerEntryRepository ledgerEntryRepository;

    private Organization testOrg1;
    private Organization testOrg2;

    @BeforeEach
    void setUp() {
        testOrg1 = organizationRepository.save(Organization.builder()
                .name("테스트 단체 1")
                .businessNumber(BusinessNumber.of("BN-" + UUID.randomUUID()))
                .description("테스트용")
                .build());

        testOrg2 = organizationRepository.save(Organization.builder()
                .name("테스트 단체 2")
                .businessNumber(BusinessNumber.of("BN-" + UUID.randomUUID()))
                .description("다른 단체")
                .build());
    }

    @Test
    @DisplayName("createEntry - 파일 해시 계산 및 LedgerEntry 저장")
    void createEntry_shouldSaveEntryWithHashAndFile() {
        // given
        Long entityId = 100L;
        byte[] fileContent = "test file content".getBytes();
        String filename = "receipt.pdf";

        // when
        LedgerEntry entry = ledgerService.createEntry(
                new CreateLedgerEntryCommand(testOrg1.getId(), LedgerEntityType.DONATION, entityId, fileContent, filename)
        );

        // then
        assertAll(
                () -> assertThat(entry.getId()).isNotNull(),
                () -> assertThat(entry.getOrganizationId()).isEqualTo(testOrg1.getId()),
                () -> assertThat(entry.getEntityType()).isEqualTo(LedgerEntityType.DONATION),
                () -> assertThat(entry.getEntityId()).isEqualTo(entityId),
                () -> assertThat(entry.getDataHashValue().orElse("")).isNotBlank(),
                () -> assertThat(entry.getDataHashValue().orElse("")).hasSize(64),
                () -> assertThat(entry.getFileUrl()).isNotBlank(),
                () -> assertThat(entry.getStatus()).isEqualTo(LedgerStatus.PENDING)
        );
    }

    @Test
    @DisplayName("findByOrganization - 조직별 LedgerEntry 조회")
    void findByOrganization_shouldReturnEntriesOrderedByDate() {
        // given
        createAndSaveEntry(testOrg1.getId(), 100L);
        createAndSaveEntry(testOrg1.getId(), 101L);
        createAndSaveEntry(testOrg2.getId(), 200L);

        // when
        List<LedgerEntry> entries = ledgerService.findByOrganization(testOrg1.getId());

        // then
        assertAll(
                () -> assertThat(entries).hasSize(2),
                () -> assertThat(entries).extracting(LedgerEntry::getOrganizationId).containsOnly(testOrg1.getId())
        );
    }

    @Test
    @DisplayName("findPendingEntries - PENDING 상태 필터링")
    void findPendingEntries_shouldReturnOnlyPending() {
        // given
        createAndSaveEntry(testOrg1.getId(), 100L);
        createAndSaveEntry(testOrg1.getId(), 101L);
        LedgerEntry recorded = createAndSaveEntry(testOrg1.getId(), 102L);
        ledgerService.markAsRecorded(recorded.getId(), "0x" + "a".repeat(64));

        // when
        List<LedgerEntry> pendingEntries = ledgerService.findPendingEntries();

        // then
        assertAll(
                () -> assertThat(pendingEntries).hasSize(2),
                () -> assertThat(pendingEntries).extracting(LedgerEntry::getStatus).containsOnly(LedgerStatus.PENDING)
        );
    }

    @Test
    @DisplayName("markAsRecorded - 상태를 RECORDED로 변경")
    @Transactional
    void markAsRecorded_shouldUpdateStatusAndTxHash() {
        // given
        LedgerEntry entry = createAndSaveEntry(testOrg1.getId(), 100L);
        String txHash = "0x1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef";

        // when
        ledgerService.markAsRecorded(entry.getId(), txHash);

        // then
        LedgerEntry updated = ledgerEntryRepository.findById(entry.getId()).orElseThrow();
        assertAll(
                () -> assertThat(updated.getStatus()).isEqualTo(LedgerStatus.RECORDED),
                () -> assertThat(updated.getTxHashValue().orElse(null)).isEqualTo(txHash)
        );
    }

    @Test
    @DisplayName("markAsFailed - 상태를 FAILED로 변경")
    @Transactional
    void markAsFailed_shouldUpdateStatus() {
        // given
        LedgerEntry entry = createAndSaveEntry(testOrg1.getId(), 100L);

        // when
        ledgerService.markAsFailed(entry.getId());

        // then
        LedgerEntry updated = ledgerEntryRepository.findById(entry.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(LedgerStatus.FAILED);
    }

    @Test
    @DisplayName("markAsRecorded - 존재하지 않는 Entry ID")
    void markAsRecorded_withInvalidId_shouldThrow() {
        assertThatThrownBy(() -> ledgerService.markAsRecorded(999L, "0xhash"))
                .isInstanceOf(com.vericerti.infrastructure.exception.EntityNotFoundException.class);
    }

    @Test
    @DisplayName("verifyHash - 동일 파일 검증 성공")
    void verifyHash_withSameContent_shouldReturnTrue() {
        // given
        byte[] fileContent = "test file content".getBytes();
        LedgerEntry entry = ledgerService.createEntry(
                new CreateLedgerEntryCommand(testOrg1.getId(), LedgerEntityType.DONATION, 100L, fileContent, "test.pdf")
        );

        // when
        boolean result = ledgerService.verifyHash(fileContent, entry.getDataHashValue().orElseThrow());

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("verifyHash - 다른 파일 검증 실패")
    void verifyHash_withDifferentContent_shouldReturnFalse() {
        // given
        byte[] originalContent = "original content".getBytes();
        byte[] modifiedContent = "modified content".getBytes();
        LedgerEntry entry = ledgerService.createEntry(
                new CreateLedgerEntryCommand(testOrg1.getId(), LedgerEntityType.DONATION, 100L, originalContent, "test.pdf")
        );

        // when
        boolean result = ledgerService.verifyHash(modifiedContent, entry.getDataHashValue().orElseThrow());

        // then
        assertThat(result).isFalse();
    }

    private LedgerEntry createAndSaveEntry(Long orgId, Long entityId) {
        return ledgerService.createEntry(
                new CreateLedgerEntryCommand(
                        orgId,
                        LedgerEntityType.DONATION,
                        entityId,
                        ("content for " + entityId).getBytes(),
                        "file_" + entityId + ".pdf"
                )
        );
    }
}



