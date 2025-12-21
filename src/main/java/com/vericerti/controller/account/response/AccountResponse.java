package com.vericerti.controller.account.response;

import com.vericerti.domain.account.entity.AccountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AccountResponse(
        Long id,
        Long organizationId,
        String accountNumber,
        String bankName,
        AccountType accountType,
        String accountHolder,
        BigDecimal balance,
        String description,
        LocalDateTime createdAt
) {}
