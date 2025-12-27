package com.vericerti.domain.exception;

/**
 * Thrown when a donation operation is not allowed.
 */
public class DonationOperationException extends DomainException {
    
    public DonationOperationException(String message) {
        super(message);
    }
    
    public static DonationOperationException alreadyCancelled() {
        return new DonationOperationException("Donation is already cancelled");
    }
    
    public static DonationOperationException cannotCancelConfirmed() {
        return new DonationOperationException("Cannot cancel a confirmed donation after 24 hours");
    }
    
    public static DonationOperationException refundNotAllowed() {
        return new DonationOperationException("Refund is not allowed for this donation");
    }
}
