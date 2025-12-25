package com.vericerti.domain.receipt.entity;

import com.vericerti.domain.common.vo.BusinessNumber;
import com.vericerti.domain.common.vo.Money;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "receipts", indexes = {
    @Index(name = "idx_receipts_org_date", columnList = "organization_id, issue_date"),
    @Index(name = "idx_receipts_account", columnList = "account_id")
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

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * 금액 조회 (하위 호환)
     */
    public BigDecimal getAmountValue() {
        return amount != null ? amount.getValue() : null;
    }

    /**
     * 사업자번호 조회 (하위 호환)
     */
    public String getMerchantBusinessNumberValue() {
        return merchantBusinessNumber != null ? merchantBusinessNumber.getValue() : null;
    }
}
