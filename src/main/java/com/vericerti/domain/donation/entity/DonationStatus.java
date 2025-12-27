package com.vericerti.domain.donation.entity;

/**
 * Status of a donation.
 */
public enum DonationStatus {
    /**
     * Donation is pending confirmation.
     */
    PENDING,
    
    /**
     * Donation is confirmed and recorded.
     */
    CONFIRMED,
    
    /**
     * Donation has been cancelled.
     */
    CANCELLED,
    
    /**
     * Refund has been requested.
     */
    REFUND_REQUESTED,
    
    /**
     * Refund has been completed.
     */
    REFUNDED
}
