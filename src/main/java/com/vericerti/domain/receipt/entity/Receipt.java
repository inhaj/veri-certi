package com.vericerti.domain.receipt.entity;

import com.vericerti.domain.common.vo.BusinessNumber;
import com.vericerti.domain.common.vo.Money;
import com.vericerti.domain.exception.ReceiptOperationException;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "receipts", indexes = {
    @Index(name = "idx_receipts_org_date", columnList = "organization_id, issue_date"),
    @Index(name = "idx_receipts_account", columnList = "account_id"),
    @Index(name = "idx_receipts_status", columnList = "status")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Receipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "account_id")
    private Long accountId;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "amount", nullable = false, precision = 15, scale = 2))
    private Money amount;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Column(nullable = false, length = 100)
    private String merchantName;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "merchant_business_number", length = 20))
    private BusinessNumber merchantBusinessNumber;

    @Column(length = 500)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReceiptCategory category;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ReceiptStatus status = ReceiptStatus.PENDING;

    @Column
    private LocalDateTime verifiedAt;

    @Column
    private LocalDateTime rejectedAt;

    @Column(length = 500)
    private String rejectionReason;

    @Column
    private LocalDateTime archivedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = ReceiptStatus.PENDING;
        }
    }

    public BigDecimal getAmountValue() {
        return amount != null ? amount.getValue() : null;
    }

    /**
     * Get business number for backward compatibility.
     */
    public String getMerchantBusinessNumberValue() {
        return merchantBusinessNumber != null ? merchantBusinessNumber.getValue() : null;
    }

    /**
     * Verify this receipt.
     * Only allowed from PENDING status.
     * 
     * @throws ReceiptOperationException if receipt is not in PENDING status
     */
    public void verify() {
        if (this.status == ReceiptStatus.VERIFIED) {
            throw ReceiptOperationException.alreadyVerified();
        }
        if (this.status == ReceiptStatus.REJECTED) {
            throw ReceiptOperationException.alreadyRejected();
        }
        if (this.status == ReceiptStatus.ARCHIVED) {
            throw ReceiptOperationException.alreadyArchived();
        }
        
        this.status = ReceiptStatus.VERIFIED;
        this.verifiedAt = LocalDateTime.now();
    }

    /**
     * Reject this receipt.
     * Only allowed from PENDING status.
     * 
     * @param reason Rejection reason
     * @throws ReceiptOperationException if receipt is not in PENDING status
     */
    public void reject(String reason) {
        if (this.status == ReceiptStatus.VERIFIED) {
            throw ReceiptOperationException.alreadyVerified();
        }
        if (this.status == ReceiptStatus.REJECTED) {
            throw ReceiptOperationException.alreadyRejected();
        }
        if (this.status == ReceiptStatus.ARCHIVED) {
            throw ReceiptOperationException.alreadyArchived();
        }
        
        this.status = ReceiptStatus.REJECTED;
        this.rejectedAt = LocalDateTime.now();
        this.rejectionReason = reason;
    }

    /**
     * Archive this receipt (close for period).
     * Only allowed from VERIFIED or REJECTED status.
     * 
     * @throws ReceiptOperationException if receipt is in PENDING status
     */
    public void archive() {
        if (this.status == ReceiptStatus.PENDING) {
            throw ReceiptOperationException.cannotArchivePending();
        }
        if (this.status == ReceiptStatus.ARCHIVED) {
            throw ReceiptOperationException.alreadyArchived();
        }
        
        this.status = ReceiptStatus.ARCHIVED;
        this.archivedAt = LocalDateTime.now();
    }

    /**
     * Reopen a rejected receipt for re-review.
     * Only allowed from REJECTED status.
     */
    public void reopenForReview() {
        if (this.status != ReceiptStatus.REJECTED) {
            throw new ReceiptOperationException("Can only reopen rejected receipts");
        }
        
        this.status = ReceiptStatus.PENDING;
        this.rejectedAt = null;
        this.rejectionReason = null;
    }

    /**
     * Check if this receipt is pending verification.
     */
    public boolean isPending() {
        return this.status == ReceiptStatus.PENDING;
    }

    /**
     * Check if this receipt has been verified.
     */
    public boolean isVerified() {
        return this.status == ReceiptStatus.VERIFIED;
    }

    /**
     * Check if this receipt can be modified.
     */
    public boolean isModifiable() {
        return this.status == ReceiptStatus.PENDING;
    }
}

