package com.vericerti.domain.common.vo;

import com.vericerti.infrastructure.exception.BusinessException;
import com.vericerti.infrastructure.exception.ErrorCode;
import jakarta.persistence.Embeddable;

@Embeddable
public record DataHash(String value) {
    
    private static final int SHA256_HEX_LENGTH = 64;
    
    public DataHash {
        if (value == null || value.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_DATA_HASH, "Data hash cannot be empty");
        }
        if (value.length() != SHA256_HEX_LENGTH) {
            throw new BusinessException(ErrorCode.INVALID_DATA_HASH, 
                    "Data hash must be " + SHA256_HEX_LENGTH + " characters (SHA-256)");
        }
    }
    
    public static DataHash of(String value) {
        return new DataHash(value);
    }
    
    public String getValue() {
        return value;
    }
}
