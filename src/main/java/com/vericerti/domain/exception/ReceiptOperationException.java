package com.vericerti.domain.exception;

/**
 * Thrown when a receipt operation is not allowed.
 */
public class ReceiptOperationException extends DomainException {
    
    public ReceiptOperationException(String message) {
        super(message);
    }
    
    public static ReceiptOperationException alreadyVerified() {
        return new ReceiptOperationException("Receipt is already verified");
    }
    
    public static ReceiptOperationException alreadyRejected() {
        return new ReceiptOperationException("Receipt is already rejected");
    }
    
    public static ReceiptOperationException alreadyArchived() {
        return new ReceiptOperationException("Receipt is already archived");
    }
    
    public static ReceiptOperationException cannotModifyVerified() {
        return new ReceiptOperationException("Cannot modify a verified receipt");
    }
    
    public static ReceiptOperationException cannotArchivePending() {
        return new ReceiptOperationException("Cannot archive a pending receipt. Verify or reject first.");
    }
}
