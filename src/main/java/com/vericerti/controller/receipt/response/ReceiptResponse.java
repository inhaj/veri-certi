package com.vericerti.controller.receipt.response;

import com.vericerti.domain.receipt.entity.ReceiptCategory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ReceiptResponse(
        Long id,
        Long organizationId,
        Long accountId,
        BigDecimal amount,
        LocalDate issueDate,
        String merchantName,
        String merchantBusinessNumber,
        String imageUrl,
        ReceiptCategory category,
        String description,
        LocalDateTime createdAt,
        String dataHash
) {}
