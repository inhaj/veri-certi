package com.vericerti.domain.donation.entity;

import com.vericerti.domain.common.vo.Money;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Donation Aggregate Root
 */
@Entity
@Table(name = "donations", indexes = {
    @Index(name = "idx_donations_org_date", columnList = "organization_id, donatedAt")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    @Column(nullable = false)
    private LocalDateTime donatedAt;

    @PrePersist
    protected void onCreate() {
        this.donatedAt = LocalDateTime.now();
    }

    /**
     * 팩토리 메서드
     */
    public static Donation create(Long organizationId, Long memberId, 
                                   BigDecimal amount, String purpose) {
        Donation donation = new Donation();
        donation.organizationId = organizationId;
        donation.memberId = memberId;
        donation.amount = Money.of(amount);
        donation.purpose = purpose;
        return donation;
    }
    
    /**
     * 금액 조회 (하위 호환)
     */
    public BigDecimal getAmountValue() {
        return amount != null ? amount.getValue() : null;
    }
}



