package com.vericerti.domain.exception;

/**
 * Thrown when an invalid data hash is provided.
 */
public class InvalidHashException extends DomainException {
    
    public InvalidHashException(String hash) {
        super("Invalid hash format: " + hash);
    }
    
    public InvalidHashException(String hash, String reason) {
        super("Invalid hash: " + hash + " - " + reason);
    }
}
