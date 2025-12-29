package com.vericerti.domain.common.vo;

import com.vericerti.domain.exception.InvalidBusinessNumberException;
import jakarta.persistence.Embeddable;

@Embeddable
public record BusinessNumber(String value) {
    
    public BusinessNumber {
        if (value == null || value.isBlank()) {
            throw new InvalidBusinessNumberException("", "Business number cannot be empty");
        }
    }
    
    public static BusinessNumber of(String value) {
        return new BusinessNumber(value);
    }
    
    public String getValue() {
        return value;
    }
}

