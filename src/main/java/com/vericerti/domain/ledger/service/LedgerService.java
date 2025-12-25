package com.vericerti.domain.ledger.service;

import com.vericerti.application.command.CreateLedgerEntryCommand;
import com.vericerti.domain.common.vo.DataHash;
import com.vericerti.domain.ledger.entity.LedgerEntry;
import com.vericerti.domain.ledger.entity.LedgerStatus;
import com.vericerti.domain.ledger.repository.LedgerEntryRepository;
import com.vericerti.domain.organization.repository.OrganizationRepository;
import com.vericerti.infrastructure.exception.EntityNotFoundException;
import com.vericerti.infrastructure.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LedgerService {

    private final LedgerEntryRepository ledgerEntryRepository;
    private final OrganizationRepository organizationRepository;
    private final FileStorageService fileStorageService;

    @Transactional
    public LedgerEntry createEntry(CreateLedgerEntryCommand command) {
        if (!organizationRepository.existsById(command.organizationId())) {
            throw EntityNotFoundException.organization(command.organizationId());
        }

        String dataHash = fileStorageService.calculateHash(command.fileContent());
        String fileUrl = fileStorageService.store(command.fileContent(), command.filename());

        LedgerEntry entry = LedgerEntry.builder()
                .organizationId(command.organizationId())
                .entityType(command.entityType())
                .entityId(command.entityId())
                .dataHash(DataHash.of(dataHash))
                .fileUrl(fileUrl)
                .build();
        LedgerEntry saved = ledgerEntryRepository.save(entry);

        log.info("event=ledger_entry_created orgId={} entryId={} entityType={}", 
                command.organizationId(), saved.getId(), command.entityType());
        return saved;
    }

    @Transactional(readOnly = true)
    public List<LedgerEntry> findByOrganization(Long organizationId) {
        if (!organizationRepository.existsById(organizationId)) {
            throw EntityNotFoundException.organization(organizationId);
        }
        return ledgerEntryRepository.findByOrganizationIdOrderByRecordedAtDesc(organizationId);
    }

    @Transactional(readOnly = true)
    public Optional<LedgerEntry> findByTxHash(String txHash) {
        return ledgerEntryRepository.findByBlockchainTxHash(txHash);
    }

    /**
     * 블록체인 동기화 배치용
     */
    @Transactional(readOnly = true)
    public List<LedgerEntry> findPendingEntries() {
        return ledgerEntryRepository.findByStatus(LedgerStatus.PENDING);
    }

    @Transactional
    public void markAsRecorded(Long entryId, String txHash) {
        LedgerEntry entry = ledgerEntryRepository.findById(entryId)
                .orElseThrow(() -> EntityNotFoundException.ledgerEntry(entryId));
        entry.markAsRecorded(txHash);
        log.info("event=ledger_recorded entryId={} txHash={}", entryId, txHash);
    }

    @Transactional
    public void markAsFailed(Long entryId) {
        LedgerEntry entry = ledgerEntryRepository.findById(entryId)
                .orElseThrow(() -> EntityNotFoundException.ledgerEntry(entryId));
        entry.markAsFailed();
        log.info("event=ledger_failed entryId={}", entryId);
    }

    /**
     * 데이터 해시로 검증
     */
    public boolean verifyHash(byte[] fileContent, String expectedHash) {
        String actualHash = fileStorageService.calculateHash(fileContent);
        return actualHash.equals(expectedHash);
    }
}


