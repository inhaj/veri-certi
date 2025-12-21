package com.vericerti.infrastructure.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 에러 코드 정의
 * 
 * 코드 패턴:
 * - U: User/Member 관련
 * - A: Auth 관련  
 * - D: Donation 관련
 * - L: Ledger 관련
 * - O: Organization 관련
 * - V: Validation 관련
 * - S: System 관련
 */
@Getter
public enum ErrorCode {
    // User/Member
    USER_NOT_FOUND("U001", "User not found", HttpStatus.NOT_FOUND),
    DUPLICATE_EMAIL("U002", "Email already exists", HttpStatus.CONFLICT),
    
    // Auth
    INVALID_CREDENTIALS("A001", "Invalid credentials", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN("A002", "Invalid token", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED("A003", "Token has expired", HttpStatus.UNAUTHORIZED),
    TOKEN_REUSE_DETECTED("A004", "Token reuse detected", HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED("A005", "Access denied", HttpStatus.FORBIDDEN),
    
    // Organization
    ORGANIZATION_NOT_FOUND("O001", "Organization not found", HttpStatus.NOT_FOUND),
    
    // Donation
    DONATION_NOT_FOUND("D001", "Donation not found", HttpStatus.NOT_FOUND),
    INVALID_DONATION_AMOUNT("D002", "Amount must be positive", HttpStatus.BAD_REQUEST),
    
    // Ledger
    LEDGER_ENTRY_NOT_FOUND("L001", "Ledger entry not found", HttpStatus.NOT_FOUND),
    INVALID_DATA_HASH("L002", "Invalid data hash format", HttpStatus.BAD_REQUEST),
    INVALID_TX_HASH("L003", "Invalid transaction hash format", HttpStatus.BAD_REQUEST),
    
    // Validation  
    VALIDATION_ERROR("V001", "Validation failed", HttpStatus.BAD_REQUEST),
    INVALID_EMAIL("V002", "Invalid email format", HttpStatus.BAD_REQUEST),
    INVALID_BUSINESS_NUMBER("V003", "Invalid business number format", HttpStatus.BAD_REQUEST),
    INVALID_AMOUNT("V004", "Invalid amount", HttpStatus.BAD_REQUEST),
    
    // System
    INTERNAL_ERROR("S001", "An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
