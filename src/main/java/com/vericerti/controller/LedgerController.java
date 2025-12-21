package com.vericerti.controller;

import com.vericerti.controller.ledger.response.LedgerResponse;
import com.vericerti.controller.ledger.response.VerifyResponse;
import com.vericerti.domain.ledger.entity.LedgerEntry;
import com.vericerti.domain.ledger.service.LedgerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class LedgerController {

    private final LedgerService ledgerService;

    /**
     * 조직의 전체 Ledger 기록 조회 (퍼블릭 API)
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
     * 트랜잭션 해시로 검증 (퍼블릭 API)
     */
    @GetMapping("/api/ledger/verify/{txHash}")
    public ResponseEntity<VerifyResponse> verifyByTxHash(@PathVariable String txHash) {
        Optional<LedgerEntry> entry = ledgerService.findByTxHash(txHash);

        return entry.map(ledgerEntry -> ResponseEntity.ok(new VerifyResponse(
                true,
                txHash,
                ledgerEntry.getDataHashValue(),
                "Transaction verified on blockchain"
        ))).orElseGet(() -> ResponseEntity.ok(new VerifyResponse(
                false, txHash, null, "Transaction not found"
        )));

    }

    private LedgerResponse toResponse(LedgerEntry entry) {
        return new LedgerResponse(
                entry.getId(),
                entry.getOrganizationId(),
                entry.getEntityType(),
                entry.getEntityId(),
                entry.getDataHashValue(),
                entry.getFileUrl(),
                entry.getBlockchainTxHash(),
                entry.getStatus(),
                entry.getRecordedAt()
        );
    }
}

