package com.vericerti.domain.common.vo;

import com.vericerti.infrastructure.exception.BusinessException;
import com.vericerti.infrastructure.exception.ErrorCode;
import jakarta.persistence.Embeddable;

@Embeddable
public record TxHash(String value) {
    
    public TxHash {
        if (value != null && !value.isBlank()) {
            if (!value.startsWith("0x")) {
                throw new BusinessException(ErrorCode.INVALID_TX_HASH, 
                        "Transaction hash must start with 0x");
            }
        }
    }
    
    public static TxHash of(String value) {
        return value == null || value.isBlank() ? null : new TxHash(value);
    }
    
    public String getValue() {
        return value;
    }
}
