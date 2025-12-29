package com.vericerti.domain.common.vo;

import com.vericerti.domain.exception.InvalidTxHashException;
import jakarta.persistence.Embeddable;

@Embeddable
public record TxHash(String value) {
    
    private static final String TX_HASH_PREFIX = "0x";
    private static final int ETHEREUM_TX_HASH_LENGTH = 66; // 0x + 64 hex chars
    
    public TxHash {
        if (value != null && !value.isBlank()) {
            if (!value.startsWith(TX_HASH_PREFIX)) {
                throw new InvalidTxHashException(value, "Transaction hash must start with 0x");
            }
            if (value.length() != ETHEREUM_TX_HASH_LENGTH) {
                throw new InvalidTxHashException(value, 
                    "Transaction hash must be " + ETHEREUM_TX_HASH_LENGTH + " characters");
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

