package com.vericerti.domain.exception;

public class InvalidAccountNumberException extends DomainException {
    
    public InvalidAccountNumberException(String accountNumber) {
        super("Invalid account number format: " + accountNumber);
    }
    
    public InvalidAccountNumberException(String accountNumber, String reason) {
        super("Invalid account number: " + accountNumber + " - " + reason);
    }
}
