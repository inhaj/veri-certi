package com.vericerti.controller.donation.response;

import com.vericerti.domain.ledger.entity.LedgerStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DonationResponse(
        Long id,
        Long organizationId,
        Long memberId,
        BigDecimal amount,
        String purpose,
        LocalDateTime donatedAt,
        LedgerInfo ledgerInfo
) {
    public record LedgerInfo(
            Long ledgerEntryId,
            String dataHash,
            String blockchainTxHash,
            LedgerStatus status
    ) {}
}
