package com.vericerti.application.dto;

import com.vericerti.domain.donation.entity.Donation;
import com.vericerti.domain.ledger.entity.LedgerEntry;
public record DonationResult(
        Donation donation,
        LedgerEntry ledgerEntry
) {}
