package com.vericerti.application.usecase;

import com.vericerti.application.command.CreateLedgerEntryCommand;
import com.vericerti.application.command.CreateReceiptCommand;
import com.vericerti.application.command.RecordReceiptCommand;
import com.vericerti.application.dto.ReceiptResult;
import com.vericerti.domain.ledger.entity.LedgerEntityType;
import com.vericerti.domain.ledger.entity.LedgerEntry;
import com.vericerti.domain.ledger.service.LedgerService;
import com.vericerti.domain.receipt.entity.Receipt;
import com.vericerti.domain.receipt.service.ReceiptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecordReceiptUseCase {

    private final ReceiptService receiptService;
    private final LedgerService ledgerService;

    @Transactional
    public ReceiptResult execute(RecordReceiptCommand command) {
        // 1. Receipt 생성
        Receipt receipt = receiptService.createReceipt(
                new CreateReceiptCommand(
                        command.organizationId(),
                        command.accountId(),
                        command.amount(),
                        command.issueDate(),
                        command.merchantName(),
                        command.merchantBusinessNumber(),
                        null,  // imageUrl - LedgerEntry에서 생성
                        command.category(),
                        command.description()
                )
        );

        // 2. LedgerEntry 생성 (영수증 이미지 해시 + 파일 저장)
        LedgerEntry ledgerEntry = ledgerService.createEntry(
                new CreateLedgerEntryCommand(
                        command.organizationId(),
                        LedgerEntityType.RECEIPT,
                        receipt.getId(),
                        command.receiptFile(),
                        command.filename()
                )
        );

        return new ReceiptResult(receipt, ledgerEntry);
    }
}
