package com.vericerti.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "blockchain.ethereum")
public class BlockchainProperties {
    private String networkUrl;
    private String contractAddress;
    private String privateKey;
}
