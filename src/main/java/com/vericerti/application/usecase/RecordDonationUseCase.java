package com.vericerti.application.usecase;

import com.vericerti.application.dto.DonationResult;
import com.vericerti.domain.donation.entity.Donation;
import com.vericerti.domain.donation.service.DonationService;
import com.vericerti.domain.ledger.entity.LedgerEntityType;
import com.vericerti.domain.ledger.entity.LedgerEntry;
import com.vericerti.domain.ledger.service.LedgerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Use Case: 기부 등록 + Ledger 기록 (블록체인 기록은 비동기 배치로 처리)
 */
@Service
@RequiredArgsConstructor
public class RecordDonationUseCase {

    private final DonationService donationService;
    private final LedgerService ledgerService;

    @Transactional
    public DonationResult execute(Long organizationId, Long memberId, BigDecimal amount,
                          String purpose, byte[] receiptFile, String filename) {
        // 1. 기부 생성
        Donation donation = donationService.createDonation(organizationId, memberId, amount, purpose);

        // 2. Ledger Entry 생성 (파일 저장 + 해시 계산)
        LedgerEntry ledgerEntry = ledgerService.createEntry(
                organizationId,
                LedgerEntityType.DONATION,
                donation.getId(),
                receiptFile,
                filename
        );

        return new DonationResult(donation, ledgerEntry);
    }
}

