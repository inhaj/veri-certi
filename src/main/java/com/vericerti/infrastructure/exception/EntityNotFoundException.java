package com.vericerti.infrastructure.exception;

/**
 * 엔티티를 찾을 수 없을 때 발생하는 예외
 */
public class EntityNotFoundException extends BusinessException {
    
    public EntityNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
    
    public EntityNotFoundException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
    
    public static EntityNotFoundException user(Long id) {
        return new EntityNotFoundException(ErrorCode.USER_NOT_FOUND, "User not found: " + id);
    }
    
    public static EntityNotFoundException user(String email) {
        return new EntityNotFoundException(ErrorCode.USER_NOT_FOUND, "User not found: " + email);
    }
    
    public static EntityNotFoundException organization(Long id) {
        return new EntityNotFoundException(ErrorCode.ORGANIZATION_NOT_FOUND, "Organization not found: " + id);
    }
    
    public static EntityNotFoundException donation(Long id) {
        return new EntityNotFoundException(ErrorCode.DONATION_NOT_FOUND, "Donation not found: " + id);
    }
    
    public static EntityNotFoundException ledgerEntry(Long id) {
        return new EntityNotFoundException(ErrorCode.LEDGER_ENTRY_NOT_FOUND, "Ledger entry not found: " + id);
    }
}
