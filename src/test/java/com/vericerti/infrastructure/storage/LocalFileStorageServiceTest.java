package com.vericerti.infrastructure.storage;

import com.vericerti.infrastructure.config.StorageProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 * LocalFileStorageService 단위 테스트.
 * Testcontainers/Docker 없이 순수하게 FileStorageService 기능을 검증합니다.
 */
class LocalFileStorageServiceTest {

    @TempDir
    Path tempDir;

    private LocalFileStorageService storageService;

    @BeforeEach
    void setUp() {
        StorageProperties properties = new StorageProperties();
        StorageProperties.LocalStorage localStorage = new StorageProperties.LocalStorage();
        localStorage.setUploadDir(tempDir.toString());
        properties.setLocal(localStorage);

        storageService = new LocalFileStorageService(properties);
        storageService.init();
    }

    @Test
    @DisplayName("calculateHash - 동일 내용은 동일 해시 반환")
    void calculateHash_sameContent_shouldReturnSameHash() {
        // given
        byte[] content = "test content".getBytes();

        // when
        String hash1 = storageService.calculateHash(content);
        String hash2 = storageService.calculateHash(content);

        // then
        assertAll(
                () -> assertThat(hash1).isEqualTo(hash2),
                () -> assertThat(hash1).hasSize(64) // SHA-256 = 64 hex chars
        );
    }

    @Test
    @DisplayName("calculateHash - 다른 내용은 다른 해시 반환")
    void calculateHash_differentContent_shouldReturnDifferentHash() {
        // given
        byte[] content1 = "content A".getBytes();
        byte[] content2 = "content B".getBytes();

        // when
        String hash1 = storageService.calculateHash(content1);
        String hash2 = storageService.calculateHash(content2);

        // then
        assertThat(hash1).isNotEqualTo(hash2);
    }

    @Test
    @DisplayName("store - 파일 저장 및 URL 반환")
    void store_shouldSaveFileAndReturnUrl() throws IOException {
        // given
        byte[] content = "file content for storage test".getBytes();
        String filename = "document.pdf";

        // when
        String fileUrl = storageService.store(content, filename);

        // then
        assertAll(
                () -> assertThat(fileUrl).startsWith("/files/"),
                () -> assertThat(fileUrl).endsWith(".pdf"));

        // 실제 파일 저장 확인
        String storedFilename = fileUrl.replace("/files/", "");
        Path storedPath = tempDir.resolve(storedFilename);
        assertThat(Files.exists(storedPath)).isTrue();
        assertThat(Files.readAllBytes(storedPath)).isEqualTo(content);
    }

    @Test
    @DisplayName("store - 확장자 없는 파일도 처리")
    void store_noExtension_shouldWork() {
        // given
        byte[] content = "no extension file".getBytes();
        String filename = "noext";

        // when
        String fileUrl = storageService.store(content, filename);

        // then
        assertThat(fileUrl).startsWith("/files/");
        assertThat(fileUrl).doesNotContain(".");
    }

    @Test
    @DisplayName("store - 여러 파일 저장 시 고유 URL 생성")
    void store_multipleFiles_shouldGenerateUniqueUrls() {
        // given
        byte[] content = "same content".getBytes();
        String filename = "test.txt";

        // when
        String url1 = storageService.store(content, filename);
        String url2 = storageService.store(content, filename);

        // then
        assertThat(url1).isNotEqualTo(url2); // UUID 기반으로 항상 다른 URL
    }
}
