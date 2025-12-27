package com.vericerti.infrastructure.blockchain;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Redis에서 블록체인 컨트랙트 주소를 읽어오는 서비스
 * Hardhat 배포 스크립트가 Redis에 저장한 주소를 가져옴
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BlockchainContractService {

    private static final String CONTRACT_ADDRESS_KEY = "blockchain:contract:address";
    private static final String CONTRACT_INFO_KEY = "blockchain:contract:info";

    private final StringRedisTemplate redisTemplate;

    public Optional<String> getContractAddress() {
        try {
            String address = redisTemplate.opsForValue().get(CONTRACT_ADDRESS_KEY);
            if (address != null && !address.isBlank()) {
                log.debug("Contract address from Redis: {}", address);
                return Optional.of(address);
            }
            log.warn("Contract address not found in Redis (key: {})", CONTRACT_ADDRESS_KEY);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Failed to get contract address from Redis: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<String> getContractInfo() {
        try {
            String info = redisTemplate.opsForValue().get(CONTRACT_INFO_KEY);
            return Optional.ofNullable(info);
        } catch (Exception e) {
            log.error("Failed to get contract info from Redis: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public boolean isContractDeployed() {
        return getContractAddress().isPresent();
    }
}
