package com.vericerti.application.command;

import com.vericerti.domain.account.entity.AccountType;

import java.math.BigDecimal;

public record CreateAccountCommand(
        Long organizationId,
        String accountNumber,
        String bankName,
        AccountType accountType,
        String accountHolder,
        BigDecimal balance,
        String description
) {}
