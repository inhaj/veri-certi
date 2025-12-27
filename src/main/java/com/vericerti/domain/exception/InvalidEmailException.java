package com.vericerti.domain.exception;

/**
 * Thrown when an invalid email format is provided.
 */
public class InvalidEmailException extends DomainException {
    
    public InvalidEmailException(String email) {
        super("Invalid email format: " + email);
    }
    
    public InvalidEmailException(String email, String reason) {
        super("Invalid email: " + email + " - " + reason);
    }
}
