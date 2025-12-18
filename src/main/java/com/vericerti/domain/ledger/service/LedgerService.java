package com.vericerti.domain.ledger.service;

import com.vericerti.domain.ledger.entity.LedgerEntry;
import com.vericerti.domain.ledger.entity.LedgerEntityType;
import com.vericerti.domain.ledger.entity.LedgerStatus;
import com.vericerti.domain.ledger.repository.LedgerEntryRepository;
import com.vericerti.infrastructure.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LedgerService {

    private final LedgerEntryRepository ledgerEntryRepository;
    private final FileStorageService fileStorageService;

    @Transactional
    public LedgerEntry createEntry(Long organizationId, LedgerEntityType entityType,
                                    Long entityId, byte[] fileContent, String filename) {
        // 파일 해시 계산
        String dataHash = fileStorageService.calculateHash(fileContent);

        // 파일 저장
        String fileUrl = fileStorageService.store(fileContent, filename);

        // LedgerEntry 생성 (PENDING 상태)
        LedgerEntry entry = LedgerEntry.builder()
                .organizationId(organizationId)
                .entityType(entityType)
                .entityId(entityId)
                .dataHash(dataHash)
                .fileUrl(fileUrl)
                .status(LedgerStatus.PENDING)
                .build();

        return ledgerEntryRepository.save(entry);
    }

    @Transactional(readOnly = true)
    public List<LedgerEntry> findByOrganization(Long organizationId) {
        return ledgerEntryRepository.findByOrganizationIdOrderByRecordedAtDesc(organizationId);
    }

    @Transactional(readOnly = true)
    public Optional<LedgerEntry> findByTxHash(String txHash) {
        return ledgerEntryRepository.findByBlockchainTxHash(txHash);
    }

    @Transactional(readOnly = true)
    public List<LedgerEntry> findPendingEntries() {
        return ledgerEntryRepository.findByStatus(LedgerStatus.PENDING);
    }

    @Transactional
    public void markAsRecorded(Long entryId, String txHash) {
        LedgerEntry entry = ledgerEntryRepository.findById(entryId)
                .orElseThrow(() -> new IllegalArgumentException("Entry not found: " + entryId));
        entry.markAsRecorded(txHash);
    }

    @Transactional
    public void markAsFailed(Long entryId) {
        LedgerEntry entry = ledgerEntryRepository.findById(entryId)
                .orElseThrow(() -> new IllegalArgumentException("Entry not found: " + entryId));
        entry.markAsFailed();
    }

    /**
     * 데이터 해시로 검증
     */
    public boolean verifyHash(byte[] fileContent, String expectedHash) {
        String actualHash = fileStorageService.calculateHash(fileContent);
        return actualHash.equals(expectedHash);
    }
}
