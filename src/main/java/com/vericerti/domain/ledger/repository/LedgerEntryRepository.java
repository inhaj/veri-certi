package com.vericerti.domain.ledger.repository;

import com.vericerti.domain.ledger.entity.LedgerEntry;
import com.vericerti.domain.ledger.entity.LedgerStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {
    List<LedgerEntry> findByOrganizationIdOrderByRecordedAtDesc(Long organizationId);
    List<LedgerEntry> findByStatus(LedgerStatus status);
    Optional<LedgerEntry> findByBlockchainTxHash(String txHash);
}
