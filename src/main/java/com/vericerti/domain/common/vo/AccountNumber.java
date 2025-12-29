package com.vericerti.domain.common.vo;

import com.vericerti.domain.exception.InvalidAccountNumberException;
import jakarta.persistence.Embeddable;

import java.util.regex.Pattern;

@Embeddable
public record AccountNumber(String value) {
    
    // Account number: digits only, 10-14 characters
    private static final Pattern DIGITS_ONLY = Pattern.compile("^[0-9]{10,14}$");
    
    public AccountNumber {
        if (value == null || value.isBlank()) {
            throw new InvalidAccountNumberException("", "Account number cannot be empty");
        }
        // Strip hyphens and whitespace
        String normalized = value.replaceAll("[\\-\\s]", "");
        
        if (!DIGITS_ONLY.matcher(normalized).matches()) {
            throw new InvalidAccountNumberException(value, 
                "Account number must be 10-14 digits");
        }
        value = normalized;
    }
    
    public static AccountNumber of(String value) {
        return new AccountNumber(value);
    }
    
    public String getValue() {
        return value;
    }
    
    /**
     * Returns formatted display value with hyphens.
     * Example: 1234567890123 -> 123-456-7890123
     */
    public String toDisplayFormat() {
        if (value.length() >= 10) {
            return value.substring(0, 3) + "-" + 
                   value.substring(3, 6) + "-" + 
                   value.substring(6);
        }
        return value;
    }
}

