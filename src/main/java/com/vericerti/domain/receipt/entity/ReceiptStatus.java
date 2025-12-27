package com.vericerti.domain.receipt.entity;

/**
 * Status of a receipt in the verification workflow.
 */
public enum ReceiptStatus {
    /**
     * Receipt is pending verification.
     */
    PENDING,
    
    /**
     * Receipt has been verified and approved.
     */
    VERIFIED,
    
    /**
     * Receipt has been rejected.
     */
    REJECTED,
    
    /**
     * Receipt has been archived (closed period).
     */
    ARCHIVED
}
