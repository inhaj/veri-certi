package com.vericerti.application.usecase;

import com.vericerti.application.command.CreateDonationCommand;
import com.vericerti.application.command.CreateLedgerEntryCommand;
import com.vericerti.application.command.RecordDonationCommand;
import com.vericerti.application.dto.DonationResult;
import com.vericerti.domain.donation.entity.Donation;
import com.vericerti.domain.donation.service.DonationService;
import com.vericerti.domain.ledger.entity.LedgerEntityType;
import com.vericerti.domain.ledger.entity.LedgerEntry;
import com.vericerti.domain.ledger.service.LedgerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecordDonationUseCase {

    private final DonationService donationService;
    private final LedgerService ledgerService;

    @Transactional
    public DonationResult execute(RecordDonationCommand command) {
        Donation donation = donationService.createDonation(
                new CreateDonationCommand(
                        command.organizationId(),
                        command.memberId(),
                        command.amount(),
                        command.purpose()
                )
        );

        LedgerEntry ledgerEntry = ledgerService.createEntry(
                new CreateLedgerEntryCommand(
                        command.organizationId(),
                        LedgerEntityType.DONATION,
                        donation.getId(),
                        command.receiptFile(),
                        command.filename()
                )
        );

        return new DonationResult(donation, ledgerEntry);
    }
}


