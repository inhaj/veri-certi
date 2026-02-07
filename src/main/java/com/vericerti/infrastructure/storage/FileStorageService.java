package com.vericerti.infrastructure.storage;

public interface FileStorageService {

    /**
     * @param content 파일 바이트 배열
     * @return 64자 hex 문자열 (SHA-256)
     */
    String calculateHash(byte[] content);

    /**
     * @param content  파일 바이트 배열
     * @param filename 원본 파일명
     * @return 저장된 파일 URL
     */
    String store(byte[] content, String filename);
}
