package com.vericerti.infrastructure.exception;

/**
 * Exception for blockchain-related errors.
 * Wraps Web3j and contract interaction failures.
 */
public class BlockchainException extends BusinessException {

    public BlockchainException(ErrorCode errorCode) {
        super(errorCode);
    }

    public BlockchainException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public BlockchainException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public BlockchainException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
