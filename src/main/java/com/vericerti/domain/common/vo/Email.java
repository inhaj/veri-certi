package com.vericerti.domain.common.vo;

import com.vericerti.infrastructure.exception.BusinessException;
import com.vericerti.infrastructure.exception.ErrorCode;
import jakarta.persistence.Embeddable;

import java.util.regex.Pattern;

@Embeddable
public record Email(String value) {
    
    private static final Pattern EMAIL_PATTERN = 
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    
    public Email {
        if (value == null || value.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_EMAIL, "Email cannot be empty");
        }
        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new BusinessException(ErrorCode.INVALID_EMAIL, "Invalid email format: " + value);
        }
    }
    
    public static Email of(String value) {
        return new Email(value);
    }
    
    public String getValue() {
        return value;
    }
}
