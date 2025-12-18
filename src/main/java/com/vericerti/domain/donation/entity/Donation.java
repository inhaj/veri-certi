package com.vericerti.domain.donation.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "donations", indexes = {
    @Index(name = "idx_donations_org_date", columnList = "organizationId, donatedAt")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Donation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long organizationId;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(length = 500)
    private String purpose;

    @Column(nullable = false)
    private LocalDateTime donatedAt;

    @PrePersist
    protected void onCreate() {
        this.donatedAt = LocalDateTime.now();
    }
}
