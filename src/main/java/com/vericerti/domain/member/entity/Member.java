package com.vericerti.domain.member.entity;

import com.vericerti.domain.common.vo.Email;
import com.vericerti.domain.exception.MemberOperationException;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Optional;

@Entity
@Table(name = "members", indexes = {
    @Index(name = "idx_members_status", columnList = "status")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "email", nullable = false, unique = true))
    private Email email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MemberStatus status = MemberStatus.ACTIVE;

    @Column
    private LocalDateTime suspendedAt;

    @Column(length = 500)
    private String suspensionReason;

    @Column
    private LocalDateTime withdrawnAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = MemberStatus.ACTIVE;
        }
    }

    /**
     * Suspend this member.
     * @param reason Suspension reason
     */
    public void suspend(String reason) {
        if (this.status == MemberStatus.SUSPENDED) {
            throw MemberOperationException.alreadySuspended();
        }
        if (this.status == MemberStatus.WITHDRAWN) {
            throw MemberOperationException.cannotModifyWithdrawn();
        }
        
        this.status = MemberStatus.SUSPENDED;
        this.suspendedAt = LocalDateTime.now();
        this.suspensionReason = reason;
    }

    /**
     * Reactivate suspended member.
     */
    public void reactivate() {
        if (this.status == MemberStatus.ACTIVE) {
            throw MemberOperationException.alreadyActive();
        }
        if (this.status == MemberStatus.WITHDRAWN) {
            throw MemberOperationException.cannotModifyWithdrawn();
        }
        
        this.status = MemberStatus.ACTIVE;
        this.suspendedAt = null;
        this.suspensionReason = null;
    }

    /**
     * Withdraw this member from the service.
     */
    public void withdraw() {
        if (this.status == MemberStatus.WITHDRAWN) {
            throw MemberOperationException.alreadyWithdrawn();
        }
        
        this.status = MemberStatus.WITHDRAWN;
        this.withdrawnAt = LocalDateTime.now();
    }

    public boolean isActive() {
        return this.status == MemberStatus.ACTIVE;
    }

    public boolean isSuspended() {
        return this.status == MemberStatus.SUSPENDED;
    }

    public boolean isWithdrawn() {
        return this.status == MemberStatus.WITHDRAWN;
    }

    public Optional<String> getEmailValue() {
        return Optional.ofNullable(email).map(Email::getValue);
    }
}

