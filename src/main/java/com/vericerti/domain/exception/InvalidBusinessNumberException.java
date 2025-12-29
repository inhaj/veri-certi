package com.vericerti.domain.exception;

/**
 * Thrown when an invalid business number format is provided.
 */
public class InvalidBusinessNumberException extends DomainException {
    
    public InvalidBusinessNumberException(String number) {
        super("Invalid business number format: " + number);
    }
    
    public InvalidBusinessNumberException(String number, String reason) {
        super("Invalid business number: " + number + " - " + reason);
    }
}
