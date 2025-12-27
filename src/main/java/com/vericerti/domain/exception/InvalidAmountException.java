package com.vericerti.domain.exception;

import java.math.BigDecimal;

/**
 * Thrown when an invalid money amount is provided.
 */
public class InvalidAmountException extends DomainException {
    
    public InvalidAmountException(BigDecimal amount) {
        super("Invalid amount: " + amount + ". Amount must be positive.");
    }
    
    public InvalidAmountException(String reason) {
        super("Invalid amount: " + reason);
    }
}
