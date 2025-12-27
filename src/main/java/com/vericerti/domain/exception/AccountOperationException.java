package com.vericerti.domain.exception;

import java.math.BigDecimal;

/**
 * Thrown when an account operation is not allowed.
 */
public class AccountOperationException extends DomainException {
    
    public AccountOperationException(String message) {
        super(message);
    }
    
    public static AccountOperationException accountNotActive() {
        return new AccountOperationException("Account is not active");
    }
    
    public static AccountOperationException accountSuspended() {
        return new AccountOperationException("Account is suspended");
    }
    
    public static AccountOperationException accountClosed() {
        return new AccountOperationException("Account is closed and cannot be reactivated");
    }
    
    public static AccountOperationException insufficientBalance(BigDecimal requested, BigDecimal available) {
        return new AccountOperationException(
            String.format("Insufficient balance. Requested: %s, Available: %s", requested, available)
        );
    }
    
    public static AccountOperationException negativeAmount() {
        return new AccountOperationException("Amount must be positive");
    }
    
    public static AccountOperationException alreadyActive() {
        return new AccountOperationException("Account is already active");
    }
    
    public static AccountOperationException alreadySuspended() {
        return new AccountOperationException("Account is already suspended");
    }
}
