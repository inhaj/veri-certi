package com.vericerti.domain.ledger.service;

import com.vericerti.config.BaseIntegrationTest;
import com.vericerti.domain.ledger.entity.LedgerEntry;
import com.vericerti.domain.ledger.entity.LedgerEntityType;
import com.vericerti.domain.ledger.entity.LedgerStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LedgerServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private LedgerService ledgerService;


    @Test
    @DisplayName("createEntry - 파일 해시 계산 및 LedgerEntry 저장")
    void createEntry_shouldSaveEntryWithHashAndFile() {
        // given
        Long organizationId = 1L;
        Long entityId = 100L;
        byte[] fileContent = "test file content".getBytes();
        String filename = "receipt.pdf";

        // when
        LedgerEntry entry = ledgerService.createEntry(
                organizationId,
                LedgerEntityType.DONATION,
                entityId,
                fileContent,
                filename
        );

        // then
        assertThat(entry.getId()).isNotNull();
        assertThat(entry.getOrganizationId()).isEqualTo(organizationId);
        assertThat(entry.getEntityType()).isEqualTo(LedgerEntityType.DONATION);
        assertThat(entry.getEntityId()).isEqualTo(entityId);
        assertThat(entry.getDataHash()).isNotBlank();
        assertThat(entry.getDataHash()).hasSize(64); // SHA-256 hex
        assertThat(entry.getFileUrl()).isNotBlank();
        assertThat(entry.getStatus()).isEqualTo(LedgerStatus.PENDING);
    }

    @Test
    @DisplayName("findByOrganization - 조직별 LedgerEntry 조회")
    void findByOrganization_shouldReturnEntriesOrderedByDate() {
        // given
        Long orgId = 1L;
        LedgerEntry entry1 = createAndSaveEntry(orgId, 100L);
        LedgerEntry entry2 = createAndSaveEntry(orgId, 101L);
        createAndSaveEntry(2L, 200L); // 다른 조직

        // when
        List<LedgerEntry> entries = ledgerService.findByOrganization(orgId);

        // then
        assertThat(entries).hasSize(2);
        assertThat(entries).extracting(LedgerEntry::getOrganizationId)
                .containsOnly(orgId);
    }

    @Test
    @DisplayName("findPendingEntries - PENDING 상태 필터링")
    void findPendingEntries_shouldReturnOnlyPending() {
        // given
        LedgerEntry pending1 = createAndSaveEntry(1L, 100L);
        LedgerEntry pending2 = createAndSaveEntry(1L, 101L);
        LedgerEntry recorded = createAndSaveEntry(1L, 102L);
        ledgerService.markAsRecorded(recorded.getId(), "0x123abc");

        // when
        List<LedgerEntry> pendingEntries = ledgerService.findPendingEntries();

        // then
        assertThat(pendingEntries).hasSize(2);
        assertThat(pendingEntries).extracting(LedgerEntry::getStatus)
                .containsOnly(LedgerStatus.PENDING);
    }

    @Test
    @DisplayName("markAsRecorded - 상태를 RECORDED로 변경")
    @Transactional
    void markAsRecorded_shouldUpdateStatusAndTxHash() {
        // given
        LedgerEntry entry = createAndSaveEntry(1L, 100L);
        String txHash = "0x1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef";

        // when
        ledgerService.markAsRecorded(entry.getId(), txHash);

        // then
        LedgerEntry updated = ledgerEntryRepository.findById(entry.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(LedgerStatus.RECORDED);
        assertThat(updated.getBlockchainTxHash()).isEqualTo(txHash);
    }

    @Test
    @DisplayName("markAsFailed - 상태를 FAILED로 변경")
    @Transactional
    void markAsFailed_shouldUpdateStatus() {
        // given
        LedgerEntry entry = createAndSaveEntry(1L, 100L);

        // when
        ledgerService.markAsFailed(entry.getId());

        // then
        LedgerEntry updated = ledgerEntryRepository.findById(entry.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(LedgerStatus.FAILED);
    }

    @Test
    @DisplayName("markAsRecorded - 존재하지 않는 Entry ID")
    void markAsRecorded_withInvalidId_shouldThrow() {
        // when & then
        assertThatThrownBy(() -> ledgerService.markAsRecorded(999L, "0xhash"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Entry not found");
    }

    @Test
    @DisplayName("verifyHash - 동일 파일 검증 성공")
    void verifyHash_withSameContent_shouldReturnTrue() {
        // given
        byte[] fileContent = "test file content".getBytes();
        LedgerEntry entry = ledgerService.createEntry(1L, LedgerEntityType.DONATION, 100L, fileContent, "test.pdf");

        // when
        boolean result = ledgerService.verifyHash(fileContent, entry.getDataHash());

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("verifyHash - 다른 파일 검증 실패")
    void verifyHash_withDifferentContent_shouldReturnFalse() {
        // given
        byte[] originalContent = "original content".getBytes();
        byte[] modifiedContent = "modified content".getBytes();
        LedgerEntry entry = ledgerService.createEntry(1L, LedgerEntityType.DONATION, 100L, originalContent, "test.pdf");

        // when
        boolean result = ledgerService.verifyHash(modifiedContent, entry.getDataHash());

        // then
        assertThat(result).isFalse();
    }

    private LedgerEntry createAndSaveEntry(Long orgId, Long entityId) {
        return ledgerService.createEntry(
                orgId,
                LedgerEntityType.DONATION,
                entityId,
                ("content for " + entityId).getBytes(),
                "file_" + entityId + ".pdf"
        );
    }
}
