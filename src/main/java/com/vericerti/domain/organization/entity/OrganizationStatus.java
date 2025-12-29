package com.vericerti.domain.organization.entity;

/**
 * Status of an organization.
 */
public enum OrganizationStatus {
    /**
     * Organization registration pending approval.
     */
    PENDING,
    
    /**
     * Organization is active and operational.
     */
    ACTIVE,
    
    /**
     * Organization is suspended due to policy violation.
     */
    SUSPENDED,
    
    /**
     * Organization has been deactivated.
     */
    DEACTIVATED
}
