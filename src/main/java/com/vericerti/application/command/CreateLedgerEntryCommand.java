package com.vericerti.application.command;

import com.vericerti.domain.ledger.entity.LedgerEntityType;

public record CreateLedgerEntryCommand(
        Long organizationId,
        LedgerEntityType entityType,
        Long entityId,
        byte[] fileContent,
        String filename
) {}
