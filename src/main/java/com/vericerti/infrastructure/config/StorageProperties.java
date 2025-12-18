package com.vericerti.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "storage")
public class StorageProperties {
    private String type; // local, s3, ipfs
    private LocalStorage local = new LocalStorage();

    @Getter
    @Setter
    public static class LocalStorage {
        private String uploadDir;
    }
}
