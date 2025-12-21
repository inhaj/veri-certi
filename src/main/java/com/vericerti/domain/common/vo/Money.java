package com.vericerti.domain.common.vo;

import com.vericerti.infrastructure.exception.BusinessException;
import com.vericerti.infrastructure.exception.ErrorCode;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;

@Embeddable
public record Money(BigDecimal value) {
    
    public Money {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.INVALID_AMOUNT, "Amount must be positive");
        }
    }
    
    public static Money of(BigDecimal value) {
        return new Money(value);
    }
    
    public BigDecimal getValue() {
        return value;
    }
    
    public Money add(Money other) {
        return new Money(this.value.add(other.value));
    }
    
    public boolean isPositive() {
        return value.compareTo(BigDecimal.ZERO) > 0;
    }
}
