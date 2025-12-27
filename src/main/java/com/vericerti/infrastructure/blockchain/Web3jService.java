package com.vericerti.infrastructure.blockchain;

import com.vericerti.infrastructure.config.BlockchainProperties;
import com.vericerti.infrastructure.exception.BlockchainException;
import com.vericerti.infrastructure.exception.ErrorCode;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.DefaultGasProvider;

import java.math.BigInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class Web3jService {

    private final BlockchainProperties blockchainProperties;
    private final BlockchainContractService blockchainContractService;

    private Web3j web3j;
    private Credentials credentials;
    private boolean initialized = false;

    @PostConstruct
    public void init() {
        String networkUrl = blockchainProperties.getNetworkUrl();
        String privateKey = blockchainProperties.getPrivateKey();

        if (networkUrl == null || networkUrl.isBlank()) {
            log.warn("Blockchain network URL not configured. Web3j disabled.");
            return;
        }

        try {
            this.web3j = Web3j.build(new HttpService(networkUrl));
            
            if (privateKey != null && !privateKey.isBlank()) {
                this.credentials = Credentials.create(privateKey);
                log.info("Web3j initialized with account: {}", credentials.getAddress());
            } else {
                log.warn("Private key not configured. Read-only mode.");
            }
            
            this.initialized = true;
            log.info("Web3j connected to: {}", networkUrl);
        } catch (Exception e) {
            log.error("Failed to initialize Web3j: {}", e.getMessage());
        }
    }

    public boolean isInitialized() {
        return initialized;
    }

    public Web3j getWeb3j() {
        return web3j;
    }

    public Credentials getCredentials() {
        return credentials;
    }

    public String getContractAddress() {
        return blockchainContractService.getContractAddress()
                .orElse(blockchainProperties.getContractAddress());
    }

    public DefaultGasProvider getGasProvider() {
        return new DefaultGasProvider();
    }

    public BigInteger getBlockNumber() {
        if (!initialized || web3j == null) {
            throw new BlockchainException(ErrorCode.BLOCKCHAIN_NOT_INITIALIZED);
        }
        try {
            return web3j.ethBlockNumber().send().getBlockNumber();
        } catch (Exception e) {
            throw new BlockchainException(ErrorCode.BLOCKCHAIN_TRANSACTION_FAILED, e);
        }
    }
}
