package com.vericerti.domain.donation.entity;

import com.vericerti.domain.common.vo.Money;
import com.vericerti.domain.exception.DonationOperationException;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Entity
@Table(name = "donations", indexes = {
    @Index(name = "idx_donations_org_date", columnList = "organization_id, donatedAt")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Donation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(nullable = false)
    private Long memberId;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "amount", nullable = false, precision = 15, scale = 2))
    private Money amount;

    @Column(length = 500)
    private String purpose;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private DonationStatus status = DonationStatus.CONFIRMED;

    @Column(nullable = false)
    private LocalDateTime donatedAt;

    @Column
    private LocalDateTime cancelledAt;

    @Column(length = 500)
    private String cancelReason;

    @Column
    private LocalDateTime refundRequestedAt;

    @PrePersist
    protected void onCreate() {
        this.donatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = DonationStatus.CONFIRMED;
        }
    }
    
    public Optional<BigDecimal> getAmountValue() {
        return Optional.ofNullable(amount).map(Money::getValue);
    }

    /**
     * Cancel this donation.
     * @param reason Cancellation reason
     * @throws DonationOperationException if donation is already cancelled
     */
    public void cancel(String reason) {
        if (this.status == DonationStatus.CANCELLED) {
            throw DonationOperationException.alreadyCancelled();
        }
        if (this.status == DonationStatus.REFUNDED) {
            throw new DonationOperationException("Cannot cancel a refunded donation");
        }
        
        this.status = DonationStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
        this.cancelReason = reason;
    }

    /**
     * Request a refund for this donation.
     * @throws DonationOperationException if refund is not allowed
     */
    public void requestRefund() {
        if (this.status == DonationStatus.CANCELLED) {
            throw new DonationOperationException("Cannot request refund for cancelled donation");
        }
        if (this.status == DonationStatus.REFUND_REQUESTED) {
            throw new DonationOperationException("Refund already requested");
        }
        if (this.status == DonationStatus.REFUNDED) {
            throw new DonationOperationException("Donation already refunded");
        }
        
        this.status = DonationStatus.REFUND_REQUESTED;
        this.refundRequestedAt = LocalDateTime.now();
    }

    /**
     * Complete the refund process.
     * @throws DonationOperationException if refund was not requested
     */
    public void completeRefund() {
        if (this.status != DonationStatus.REFUND_REQUESTED) {
            throw new DonationOperationException("Refund was not requested");
        }
        
        this.status = DonationStatus.REFUNDED;
    }

    public boolean isCancellable() {
        return this.status == DonationStatus.CONFIRMED || this.status == DonationStatus.PENDING;
    }

    public boolean isRefundable() {
        return this.status == DonationStatus.CONFIRMED;
    }
}

