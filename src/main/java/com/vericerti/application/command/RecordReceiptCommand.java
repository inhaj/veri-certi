package com.vericerti.application.command;

import com.vericerti.domain.receipt.entity.ReceiptCategory;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 영수증 생성 + LedgerEntry 기록을 위한 Command
 */
public record RecordReceiptCommand(
        Long organizationId,
        Long accountId,
        BigDecimal amount,
        LocalDate issueDate,
        String merchantName,
        String merchantBusinessNumber,
        ReceiptCategory category,
        String description,
        // 영수증 이미지 파일
        byte[] receiptFile,
        String filename
) {}
