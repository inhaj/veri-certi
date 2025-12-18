package com.vericerti.domain.ledger.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ledger_entries", indexes = {
    @Index(name = "idx_ledger_tx_hash", columnList = "blockchainTxHash"),
    @Index(name = "idx_ledger_org_date", columnList = "organizationId, recordedAt")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class LedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long organizationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LedgerEntityType entityType;

    @Column(nullable = false)
    private Long entityId;

    @Column(nullable = false, length = 64)
    private String dataHash; // SHA-256

    @Column(length = 500)
    private String fileUrl; // S3 URL 또는 IPFS CID

    @Column(length = 66)
    private String blockchainTxHash; // 0x + 64자

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LedgerStatus status;

    @Column(nullable = false)
    private LocalDateTime recordedAt;

    @PrePersist
    protected void onCreate() {
        this.recordedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = LedgerStatus.PENDING;
        }
    }

    public void markAsRecorded(String txHash) {
        this.blockchainTxHash = txHash;
        this.status = LedgerStatus.RECORDED;
    }

    public void markAsFailed() {
        this.status = LedgerStatus.FAILED;
    }
}
