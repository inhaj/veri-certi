package com.vericerti.domain.exception;

/**
 * Base exception for all domain layer exceptions.
 * Domain layer should not depend on infrastructure layer exceptions.
 */
public abstract class DomainException extends RuntimeException {
    
    protected DomainException(String message) {
        super(message);
    }
    
    protected DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
