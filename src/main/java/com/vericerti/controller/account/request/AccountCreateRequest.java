package com.vericerti.controller.account.request;

import com.vericerti.domain.account.entity.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AccountCreateRequest(
        @NotBlank(message = "Account number is required")
        String accountNumber,
        
        @NotBlank(message = "Bank name is required")
        String bankName,
        
        @NotNull(message = "Account type is required")
        AccountType accountType,
        
        @NotBlank(message = "Account holder is required")
        String accountHolder,
        
        BigDecimal balance,
        
        String description
) {}
