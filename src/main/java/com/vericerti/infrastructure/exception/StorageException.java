package com.vericerti.infrastructure.exception;

/**
 * 파일 저장소 관련 예외
 */
public class StorageException extends BusinessException {

    public StorageException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public StorageException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    public static StorageException storeFailed(String filename, Throwable cause) {
        return new StorageException(
                ErrorCode.FILE_STORE_FAILED,
                "Failed to store file: " + filename,
                cause
        );
    }

    public static StorageException loadFailed(String fileUrl, Throwable cause) {
        return new StorageException(
                ErrorCode.FILE_LOAD_FAILED,
                "Failed to load file: " + fileUrl,
                cause
        );
    }
}
