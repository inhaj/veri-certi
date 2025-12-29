package com.vericerti.domain.organization.entity;

import com.vericerti.domain.common.vo.BusinessNumber;
import com.vericerti.domain.exception.OrganizationOperationException;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "organizations", indexes = {
    @Index(name = "idx_organizations_status", columnList = "status")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "business_number", unique = true))
    private BusinessNumber businessNumber;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private OrganizationStatus status = OrganizationStatus.PENDING;

    @Column
    private LocalDateTime approvedAt;

    @Column
    private LocalDateTime suspendedAt;

    @Column(length = 500)
    private String suspensionReason;

    @Column
    private LocalDateTime deactivatedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = OrganizationStatus.PENDING;
        }
    }

    /**
     * Update organization information.
     * Cannot modify deactivated organizations.
     */
    public void update(String name, String description) {
        if (this.status == OrganizationStatus.DEACTIVATED) {
            throw OrganizationOperationException.cannotModifyDeactivated();
        }
        
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        if (description != null) {
            this.description = description;
        }
    }

    /**
     * Approve pending organization.
     */
    public void approve() {
        if (this.status != OrganizationStatus.PENDING) {
            throw OrganizationOperationException.cannotApproveNonPending();
        }
        
        this.status = OrganizationStatus.ACTIVE;
        this.approvedAt = LocalDateTime.now();
    }

    /**
     * Suspend organization.
     * @param reason Suspension reason
     */
    public void suspend(String reason) {
        if (this.status == OrganizationStatus.SUSPENDED) {
            throw OrganizationOperationException.alreadySuspended();
        }
        if (this.status == OrganizationStatus.DEACTIVATED) {
            throw OrganizationOperationException.alreadyDeactivated();
        }
        
        this.status = OrganizationStatus.SUSPENDED;
        this.suspendedAt = LocalDateTime.now();
        this.suspensionReason = reason;
    }

    /**
     * Reactivate suspended organization.
     */
    public void reactivate() {
        if (this.status == OrganizationStatus.ACTIVE) {
            throw OrganizationOperationException.alreadyActive();
        }
        if (this.status == OrganizationStatus.DEACTIVATED) {
            throw OrganizationOperationException.alreadyDeactivated();
        }
        
        this.status = OrganizationStatus.ACTIVE;
        this.suspendedAt = null;
        this.suspensionReason = null;
    }

    /**
     * Permanently deactivate organization.
     */
    public void deactivate() {
        if (this.status == OrganizationStatus.DEACTIVATED) {
            throw OrganizationOperationException.alreadyDeactivated();
        }
        
        this.status = OrganizationStatus.DEACTIVATED;
        this.deactivatedAt = LocalDateTime.now();
    }

    public boolean isActive() {
        return this.status == OrganizationStatus.ACTIVE;
    }

    public boolean isSuspended() {
        return this.status == OrganizationStatus.SUSPENDED;
    }

    public boolean isPending() {
        return this.status == OrganizationStatus.PENDING;
    }

    public String getBusinessNumberValue() {
        return businessNumber != null ? businessNumber.getValue() : null;
    }
}

