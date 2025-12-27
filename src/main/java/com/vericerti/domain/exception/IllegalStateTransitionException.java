package com.vericerti.domain.exception;

/**
 * Thrown when an illegal state transition is attempted on an entity.
 */
public class IllegalStateTransitionException extends DomainException {
    
    public IllegalStateTransitionException(String fromState, String toState) {
        super("Cannot transition from " + fromState + " to " + toState);
    }
    
    public IllegalStateTransitionException(String message) {
        super(message);
    }
}
