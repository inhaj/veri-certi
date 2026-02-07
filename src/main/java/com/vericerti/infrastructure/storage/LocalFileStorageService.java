package com.vericerti.infrastructure.storage;

import com.vericerti.infrastructure.config.StorageProperties;
import com.vericerti.infrastructure.exception.ErrorCode;
import com.vericerti.infrastructure.exception.StorageException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocalFileStorageService implements FileStorageService {

    private static final String HASH_ALGORITHM = "SHA-256";

    private final StorageProperties storageProperties;
    private Path uploadPath;

    @PostConstruct
    public void init() {
        String uploadDir = storageProperties.getLocal().getUploadDir();
        this.uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(uploadPath);
            log.info("event=storage_initialized path={}", uploadPath);
        } catch (IOException e) {
            throw new StorageException(
                    ErrorCode.STORAGE_INITIALIZATION_FAILED,
                    "Could not create upload directory: " + uploadPath,
                    e);
        }
    }

    @Override
    public String calculateHash(byte[] content) {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] hashBytes = digest.digest(content);
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is always available in standard JDK
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    @Override
    public String store(byte[] content, String filename) {
        String extension = extractExtension(filename);
        String storedFilename = UUID.randomUUID() + extension;
        Path targetPath = uploadPath.resolve(storedFilename);

        try {
            Files.write(targetPath, content);
            log.info("event=file_stored original={} stored={} size={}",
                    filename, storedFilename, content.length);

            // 상대 경로를 URL 형태로 반환 (로컬 저장소)
            return "/files/" + storedFilename;
        } catch (IOException e) {
            throw StorageException.storeFailed(filename, e);
        }
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}
