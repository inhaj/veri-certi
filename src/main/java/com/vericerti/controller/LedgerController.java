package com.vericerti.controller;

import com.vericerti.controller.ledger.response.LedgerResponse;
import com.vericerti.controller.ledger.response.VerifyResponse;
import com.vericerti.domain.ledger.entity.LedgerEntry;
import com.vericerti.domain.ledger.service.LedgerService;
import com.vericerti.infrastructure.blockchain.BlockchainSyncScheduler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class LedgerController {

    private final LedgerService ledgerService;
    private final BlockchainSyncScheduler blockchainSyncScheduler;

    /**
     * Get all ledger entries for an organization (Public API)
     */
    @GetMapping("/api/organizations/{orgId}/ledger")
    public ResponseEntity<List<LedgerResponse>> getLedgerEntries(@PathVariable Long orgId) {
        List<LedgerEntry> entries = ledgerService.findByOrganization(orgId);
        List<LedgerResponse> responses = entries.stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    /**
     * Verify by transaction hash (Public API)
     */
    @GetMapping("/api/ledger/verify/{txHash}")
    public ResponseEntity<VerifyResponse> verifyByTxHash(@PathVariable String txHash) {
        Optional<LedgerEntry> entry = ledgerService.findByTxHash(txHash);

        return entry.map(ledgerEntry -> ResponseEntity.ok(new VerifyResponse(
                true,
                txHash,
                ledgerEntry.getDataHashValue().orElse(null),
                "Transaction verified on blockchain"
        ))).orElseGet(() -> ResponseEntity.ok(new VerifyResponse(
                false, txHash, null, "Transaction not found"
        )));
    }

    /**
     * Full blockchain sync (Admin only)
     */
    @PostMapping("/api/ledger/sync")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> syncAll() {
        BlockchainSyncScheduler.SyncResult result = blockchainSyncScheduler.syncAll();
        return ResponseEntity.ok(Map.of(
                "total", result.total(),
                "verified", result.verified(),
                "failed", result.failed()
        ));
    }

    /**
     * Sync specific entry (Admin only)
     */
    @PostMapping("/api/ledger/sync/{entryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> syncEntry(@PathVariable Long entryId) {
        boolean verified = blockchainSyncScheduler.syncEntry(entryId);
        return ResponseEntity.ok(Map.of(
                "entryId", entryId,
                "verified", verified
        ));
    }

    private LedgerResponse toResponse(LedgerEntry entry) {
        return new LedgerResponse(
                entry.getId(),
                entry.getOrganizationId(),
                entry.getEntityType(),
                entry.getEntityId(),
                entry.getDataHashValue().orElse(null),
                entry.getFileUrl(),
                entry.getTxHashValue().orElse(null),
                entry.getStatus(),
                entry.getRecordedAt()
        );
    }
}
