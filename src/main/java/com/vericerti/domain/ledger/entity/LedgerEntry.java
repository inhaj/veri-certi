package com.vericerti.domain.ledger.entity;

import com.vericerti.domain.common.vo.DataHash;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * LedgerEntry Aggregate Root
 */
@Entity
@Table(name = "ledger_entries", indexes = {
    @Index(name = "idx_ledger_tx_hash", columnList = "blockchain_tx_hash"),
    @Index(name = "idx_ledger_org_date", columnList = "organization_id, recordedAt")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LedgerEntityType entityType;

    @Column(nullable = false)
    private Long entityId;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "data_hash", nullable = false, length = 64))
    private DataHash dataHash;

    @Column(length = 500)
    private String fileUrl;

    @Column(name = "blockchain_tx_hash", length = 66)
    private String blockchainTxHash;

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

    /**
     * 팩토리 메서드
     */
    public static LedgerEntry create(Long organizationId, LedgerEntityType entityType,
                                      Long entityId, String dataHash, String fileUrl) {
        LedgerEntry entry = new LedgerEntry();
        entry.organizationId = organizationId;
        entry.entityType = entityType;
        entry.entityId = entityId;
        entry.dataHash = DataHash.of(dataHash);
        entry.fileUrl = fileUrl;
        entry.status = LedgerStatus.PENDING;
        return entry;
    }
    
    /**
     * 해시값 조회 (하위 호환)
     */
    public String getDataHashValue() {
        return dataHash != null ? dataHash.getValue() : null;
    }

    public void markAsRecorded(String txHash) {
        this.blockchainTxHash = txHash;
        this.status = LedgerStatus.RECORDED;
    }

    public void markAsFailed() {
        this.status = LedgerStatus.FAILED;
    }
}



