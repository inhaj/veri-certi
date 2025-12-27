package com.vericerti.domain.common.vo;

import com.vericerti.domain.exception.InvalidHashException;
import jakarta.persistence.Embeddable;

@Embeddable
public record DataHash(String value) {
    
    private static final int SHA256_HEX_LENGTH = 64;
    
    public DataHash {
        if (value == null || value.isBlank()) {
            throw new InvalidHashException("", "Data hash cannot be empty");
        }
        if (value.length() != SHA256_HEX_LENGTH) {
            throw new InvalidHashException(value, 
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

