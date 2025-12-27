package com.vericerti.domain.ledger.entity;

import com.vericerti.domain.common.vo.DataHash;
import com.vericerti.domain.exception.IllegalStateTransitionException;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Optional;

@Entity
@Table(name = "ledger_entries", indexes = {
    @Index(name = "idx_ledger_tx_hash", columnList = "blockchain_tx_hash"),
    @Index(name = "idx_ledger_org_date", columnList = "organization_id, recordedAt"),
    @Index(name = "idx_ledger_status", columnList = "status")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
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
    @Builder.Default
    private LedgerStatus status = LedgerStatus.PENDING;

    @Column(nullable = false)
    private LocalDateTime recordedAt;

    @PrePersist
    protected void onCreate() {
        this.recordedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = LedgerStatus.PENDING;
        }
    }
    
    public Optional<String> getDataHashValue() {
        return Optional.ofNullable(dataHash).map(DataHash::getValue);
    }

    /**
     * Mark this entry as recorded on the blockchain.
     * Only allowed from PENDING status.
     *
     * @param txHash Blockchain transaction hash
     * @throws IllegalStateTransitionException if current status is not PENDING
     */
    public void markAsRecorded(String txHash) {
        if (this.status != LedgerStatus.PENDING) {
            throw new IllegalStateTransitionException(
                this.status.name(), 
                LedgerStatus.RECORDED.name()
            );
        }
        if (txHash == null || txHash.isBlank()) {
            throw new IllegalArgumentException("Transaction hash cannot be empty");
        }
        
        this.blockchainTxHash = txHash;
        this.status = LedgerStatus.RECORDED;
    }

    /**
     * Mark this entry as failed.
     * Only allowed from PENDING status.
     * 
     * @throws IllegalStateTransitionException if current status is not PENDING
     */
    public void markAsFailed() {
        if (this.status != LedgerStatus.PENDING) {
            throw new IllegalStateTransitionException(
                this.status.name(), 
                LedgerStatus.FAILED.name()
            );
        }
        
        this.status = LedgerStatus.FAILED;
    }

    /**
     * Retry a failed entry by resetting to PENDING.
     * Only allowed from FAILED status.
     * 
     * @throws IllegalStateTransitionException if current status is not FAILED
     */
    public void retry() {
        if (this.status != LedgerStatus.FAILED) {
            throw new IllegalStateTransitionException(
                this.status.name(), 
                LedgerStatus.PENDING.name()
            );
        }
        
        this.status = LedgerStatus.PENDING;
        this.blockchainTxHash = null;
    }

    public boolean isPending() {
        return this.status == LedgerStatus.PENDING;
    }

    public boolean isRecorded() {
        return this.status == LedgerStatus.RECORDED;
    }
}

