package com.vericerti.infrastructure.blockchain;

import com.vericerti.domain.ledger.entity.LedgerEntry;
import com.vericerti.domain.ledger.service.LedgerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 블록체인 제출 스케줄러
 * - PENDING 상태 엔트리를 블록체인에 제출
 * - DB 상태는 변경하지 않음 (검증 후 Sync 스케줄러가 처리)
 * - Redis SET에 제출 정보 저장
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BlockchainSubmitScheduler {

    private static final String PENDING_VERIFICATION_KEY = "blockchain:pending:verification";

    private final LedgerService ledgerService;
    private final Web3jService web3jService;
    private final LedgerRegistryService ledgerRegistryService;
    private final StringRedisTemplate redisTemplate;

    /**
     * PENDING 상태의 LedgerEntry를 블록체인에 제출 (1분마다)
     * DB 상태는 변경하지 않음 - 검증 후에 Sync 스케줄러가 변경
     */
    @Scheduled(fixedDelay = 60000)
    public void submitPendingToBlockchain() {
        if (!web3jService.isInitialized()) {
            log.debug("Web3j not initialized. Skipping blockchain submit.");
            return;
        }

        String contractAddress = web3jService.getContractAddress();
        if (contractAddress == null || contractAddress.isBlank()) {
            log.debug("Contract address not configured. Skipping blockchain submit.");
            return;
        }

        List<LedgerEntry> pendingEntries = ledgerService.findPendingEntries();

        if (pendingEntries.isEmpty()) {
            return;
        }

        log.info("Found {} pending entries to submit to blockchain", pendingEntries.size());

        for (LedgerEntry entry : pendingEntries) {
            // 이미 제출 대기 중인지 확인
            if (isAlreadySubmitted(entry.getId())) {
                log.debug("Entry {} already submitted, skipping", entry.getId());
                continue;
            }

            try {
                String dataHash = entry.getDataHashValue().orElseThrow();
                if (!dataHash.startsWith("0x")) {
                    dataHash = "0x" + dataHash;
                }

                // 블록체인에 제출
                String txHash = ledgerRegistryService.registerHash(
                        dataHash,
                        entry.getOrganizationId()
                );

                // SET에 저장 (entryId:txHash:retryCount)
                String member = entry.getId() + ":" + txHash + ":0";
                redisTemplate.opsForSet().add(PENDING_VERIFICATION_KEY, member);
                
                log.info("Entry {} submitted to blockchain. TxHash: {}. Added to verification queue.", 
                        entry.getId(), txHash);

            } catch (Exception e) {
                log.error("Failed to submit entry {} to blockchain: {}", entry.getId(), e.getMessage());
            }
        }
    }

    private boolean isAlreadySubmitted(Long entryId) {
        var members = redisTemplate.opsForSet().members(PENDING_VERIFICATION_KEY);
        if (members == null) return false;
        
        String prefix = entryId + ":";
        return members.stream().anyMatch(m -> m.startsWith(prefix));
    }
}
