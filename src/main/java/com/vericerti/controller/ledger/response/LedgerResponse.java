package com.vericerti.controller.ledger.response;

import com.vericerti.domain.ledger.entity.LedgerEntityType;
import com.vericerti.domain.ledger.entity.LedgerStatus;

import java.time.LocalDateTime;

public record LedgerResponse(
        Long id,
        Long organizationId,
        LedgerEntityType entityType,
        Long entityId,
        String dataHash,
        String fileUrl,
        String blockchainTxHash,
        LedgerStatus status,
        LocalDateTime recordedAt
) {}
