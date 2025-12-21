package com.vericerti.domain.organization.entity;

import com.vericerti.domain.common.vo.BusinessNumber;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Organization Aggregate Root
 */
@Entity
@Table(name = "organizations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "business_number", unique = true))
    private BusinessNumber businessNumber;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
    
    /**
     * 사업자번호 조회 (하위 호환)
     */
    public String getBusinessNumberValue() {
        return businessNumber != null ? businessNumber.getValue() : null;
    }
}



