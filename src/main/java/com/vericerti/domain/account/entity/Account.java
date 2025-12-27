package com.vericerti.domain.account.entity;

import com.vericerti.domain.common.vo.Money;
import com.vericerti.domain.exception.AccountOperationException;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "accounts", indexes = {
    @Index(name = "idx_accounts_org", columnList = "organization_id"),
    @Index(name = "idx_accounts_status", columnList = "status")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(nullable = false, length = 50)
    private String accountNumber;

    @Column(nullable = false, length = 50)
    private String bankName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType accountType;

    @Column(nullable = false, length = 50)
    private String accountHolder;

    @Column(precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AccountStatus status = AccountStatus.ACTIVE;

    @Column(length = 500)
    private String description;

    @Column
    private LocalDateTime suspendedAt;

    @Column(length = 500)
    private String suspensionReason;

    @Column
    private LocalDateTime closedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.balance == null) {
            this.balance = BigDecimal.ZERO;
        }
        if (this.status == null) {
            this.status = AccountStatus.ACTIVE;
        }
    }

    /**
     * Deposit money into this account.
     * 
     * @param amount Amount to deposit (must be positive)
     * @throws AccountOperationException if account is not active or amount is invalid
     */
    public void deposit(Money amount) {
        validateActiveStatus();
        
        this.balance = this.balance.add(amount.getValue());
    }

    /**
     * Withdraw money from this account.
     * 
     * @param amount Amount to withdraw (must be positive)
     * @throws AccountOperationException if account is not active, insufficient balance, or amount is invalid
     */
    public void withdraw(Money amount) {
        validateActiveStatus();
        
        if (this.balance.compareTo(amount.getValue()) < 0) {
            throw AccountOperationException.insufficientBalance(amount.getValue(), this.balance);
        }
        
        this.balance = this.balance.subtract(amount.getValue());
    }

    /**
     * Suspend this account.
     * 
     * @param reason Suspension reason
     * @throws AccountOperationException if account is already suspended or closed
     */
    public void suspend(String reason) {
        if (this.status == AccountStatus.SUSPENDED) {
            throw AccountOperationException.alreadySuspended();
        }
        if (this.status == AccountStatus.CLOSED) {
            throw AccountOperationException.accountClosed();
        }
        
        this.status = AccountStatus.SUSPENDED;
        this.suspendedAt = LocalDateTime.now();
        this.suspensionReason = reason;
    }

    /**
     * Activate this account.
     * 
     * @throws AccountOperationException if account is closed or already active
     */
    public void activate() {
        if (this.status == AccountStatus.ACTIVE) {
            throw AccountOperationException.alreadyActive();
        }
        if (this.status == AccountStatus.CLOSED) {
            throw AccountOperationException.accountClosed();
        }
        
        this.status = AccountStatus.ACTIVE;
        this.suspendedAt = null;
        this.suspensionReason = null;
    }

    /**
     * Close this account permanently.
     * 
     * @throws AccountOperationException if account is already closed
     */
    public void close() {
        if (this.status == AccountStatus.CLOSED) {
            throw AccountOperationException.accountClosed();
        }
        
        this.status = AccountStatus.CLOSED;
        this.closedAt = LocalDateTime.now();
    }

    /**
     * Sync balance from external bank system.
     * This is an internal operation that bypasses normal deposit/withdraw rules.
     * 
     * @param newBalance The synced balance from bank API
     */
    public void syncBalance(BigDecimal newBalance) {
        this.balance = newBalance != null ? newBalance : BigDecimal.ZERO;
    }

    public boolean isActive() {
        return this.status == AccountStatus.ACTIVE;
    }

    public boolean isSuspended() {
        return this.status == AccountStatus.SUSPENDED;
    }

    public boolean isClosed() {
        return this.status == AccountStatus.CLOSED;
    }

    private void validateActiveStatus() {
        if (this.status == AccountStatus.SUSPENDED) {
            throw AccountOperationException.accountSuspended();
        }
        if (this.status == AccountStatus.CLOSED) {
            throw AccountOperationException.accountClosed();
        }
        if (this.status != AccountStatus.ACTIVE) {
            throw AccountOperationException.accountNotActive();
        }
    }
}

