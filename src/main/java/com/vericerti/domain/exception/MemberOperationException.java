package com.vericerti.domain.exception;

/**
 * Thrown when a member operation is not allowed.
 */
public class MemberOperationException extends DomainException {
    
    public MemberOperationException(String message) {
        super(message);
    }
    
    public static MemberOperationException alreadyActive() {
        return new MemberOperationException("Member is already active");
    }
    
    public static MemberOperationException alreadySuspended() {
        return new MemberOperationException("Member is already suspended");
    }
    
    public static MemberOperationException alreadyWithdrawn() {
        return new MemberOperationException("Member has already withdrawn");
    }
    
    public static MemberOperationException cannotModifyWithdrawn() {
        return new MemberOperationException("Cannot modify a withdrawn member");
    }
}
