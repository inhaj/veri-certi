package com.vericerti.domain.account.entity;

/**
 * Status of an account.
 */
public enum AccountStatus {
    /**
     * Account is active and can be used.
     */
    ACTIVE,
    
    /**
     * Account is inactive (not in use but not suspended).
     */
    INACTIVE,
    
    /**
     * Account is suspended due to policy violation or investigation.
     */
    SUSPENDED,
    
    /**
     * Account is closed and cannot be reactivated.
     */
    CLOSED
}
