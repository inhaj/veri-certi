package com.vericerti.infrastructure.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Error Code Definitions
 * 
 * Code patterns:
 * - U: User/Member related
 * - A: Auth related  
 * - D: Donation related
 * - L: Ledger related
 * - O: Organization related
 * - V: Validation related
 * - BC: Blockchain related
 * - S: System related
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
    
    // Account
    ACCOUNT_NOT_FOUND("AC001", "Account not found", HttpStatus.NOT_FOUND),
    
    // Receipt
    RECEIPT_NOT_FOUND("R001", "Receipt not found", HttpStatus.NOT_FOUND),
    
    // Validation  
    VALIDATION_ERROR("V001", "Validation failed", HttpStatus.BAD_REQUEST),
    INVALID_EMAIL("V002", "Invalid email format", HttpStatus.BAD_REQUEST),
    INVALID_BUSINESS_NUMBER("V003", "Invalid business number format", HttpStatus.BAD_REQUEST),
    INVALID_AMOUNT("V004", "Invalid amount", HttpStatus.BAD_REQUEST),
    
    // Common
    ENTITY_NOT_FOUND("C001", "Entity not found", HttpStatus.NOT_FOUND),
    DUPLICATE_ENTRY("C002", "Duplicate entry", HttpStatus.CONFLICT),
    
    // Storage
    FILE_STORE_FAILED("ST001", "Failed to store file", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_LOAD_FAILED("ST002", "Failed to load file", HttpStatus.INTERNAL_SERVER_ERROR),
    STORAGE_INITIALIZATION_FAILED("ST003", "Failed to initialize storage", HttpStatus.INTERNAL_SERVER_ERROR),
    
    // Blockchain
    BLOCKCHAIN_NOT_INITIALIZED("BC001", "Blockchain service not initialized", HttpStatus.SERVICE_UNAVAILABLE),
    BLOCKCHAIN_CONTRACT_NOT_CONFIGURED("BC002", "Blockchain contract address not configured", HttpStatus.SERVICE_UNAVAILABLE),
    BLOCKCHAIN_INVALID_HASH("BC003", "Invalid hash format for blockchain", HttpStatus.BAD_REQUEST),
    BLOCKCHAIN_TRANSACTION_FAILED("BC004", "Blockchain transaction failed", HttpStatus.INTERNAL_SERVER_ERROR),
    BLOCKCHAIN_VERIFICATION_FAILED("BC005", "Blockchain verification failed", HttpStatus.INTERNAL_SERVER_ERROR),
    
    // System
    INTERNAL_ERROR("S001", "An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR),
    CRYPTO_ALGORITHM_NOT_AVAILABLE("S002", "Required cryptographic algorithm not available", HttpStatus.INTERNAL_SERVER_ERROR),
    JWT_SECRET_INVALID("S003", "JWT secret configuration is invalid", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
