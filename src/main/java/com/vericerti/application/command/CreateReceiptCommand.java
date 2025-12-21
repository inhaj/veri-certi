package com.vericerti.application.command;

import com.vericerti.domain.receipt.entity.ReceiptCategory;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateReceiptCommand(
        Long organizationId,
        Long accountId,
        BigDecimal amount,
        LocalDate issueDate,
        String merchantName,
        String merchantBusinessNumber,
        String imageUrl,
        ReceiptCategory category,
        String description
) {}
