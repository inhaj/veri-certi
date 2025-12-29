package com.vericerti.domain.exception;

public class InvalidTxHashException extends DomainException {
    
    public InvalidTxHashException(String hash) {
        super("Invalid transaction hash format: " + hash);
    }
    
    public InvalidTxHashException(String hash, String reason) {
        super("Invalid transaction hash: " + hash + " - " + reason);
    }
}
