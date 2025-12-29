package com.vericerti.domain.exception;

/**
 * Thrown when an organization operation is not allowed.
 */
public class OrganizationOperationException extends DomainException {
    
    public OrganizationOperationException(String message) {
        super(message);
    }
    
    public static OrganizationOperationException alreadyActive() {
        return new OrganizationOperationException("Organization is already active");
    }
    
    public static OrganizationOperationException alreadySuspended() {
        return new OrganizationOperationException("Organization is already suspended");
    }
    
    public static OrganizationOperationException alreadyDeactivated() {
        return new OrganizationOperationException("Organization is already deactivated");
    }
    
    public static OrganizationOperationException cannotApproveNonPending() {
        return new OrganizationOperationException("Only pending organizations can be approved");
    }
    
    public static OrganizationOperationException cannotModifyDeactivated() {
        return new OrganizationOperationException("Cannot modify a deactivated organization");
    }
}
