package com.vericerti.domain.member.entity;

/**
 * Status of a member account.
 */
public enum MemberStatus {
    /**
     * Member is active and can use the system.
     */
    ACTIVE,
    
    /**
     * Member is suspended due to policy violation.
     */
    SUSPENDED,
    
    /**
     * Member has withdrawn from the service.
     */
    WITHDRAWN
}
