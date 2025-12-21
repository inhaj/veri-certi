package com.vericerti.controller.receipt.request;

import com.vericerti.domain.receipt.entity.ReceiptCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ReceiptCreateRequest(
        Long accountId,
        
        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be positive")
        BigDecimal amount,
        
        @NotNull(message = "Issue date is required")
        LocalDate issueDate,
        
        @NotBlank(message = "Merchant name is required")
        String merchantName,
        
        String merchantBusinessNumber,
        
        @NotNull(message = "Category is required")
        ReceiptCategory category,
        
        String description
) {}
