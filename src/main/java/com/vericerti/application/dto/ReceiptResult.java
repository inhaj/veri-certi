package com.vericerti.application.dto;

import com.vericerti.domain.ledger.entity.LedgerEntry;
import com.vericerti.domain.receipt.entity.Receipt;

public record ReceiptResult(
        Receipt receipt,
        LedgerEntry ledgerEntry
) {}
