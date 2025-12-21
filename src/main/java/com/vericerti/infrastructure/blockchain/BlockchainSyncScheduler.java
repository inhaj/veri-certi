package com.vericerti.infrastructure.blockchain;

import com.vericerti.domain.ledger.entity.LedgerEntry;
import com.vericerti.domain.ledger.service.LedgerService;
import com.vericerti.infrastructure.config.BlockchainProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BlockchainSyncScheduler {

    private final LedgerService ledgerService;
    private final BlockchainProperties blockchainProperties;

    /**
     * PENDING 상태의 LedgerEntry를 블록체인에 기록하는 배치
     * 1분마다 실행
     */
    @Scheduled(fixedDelay = 60000)
    public void syncPendingToBlockchain() {
        List<LedgerEntry> pendingEntries = ledgerService.findPendingEntries();

        if (pendingEntries.isEmpty()) {
            return;
        }

        log.info("Found {} pending entries to sync to blockchain", pendingEntries.size());

        // 블록체인 네트워크 설정이 없으면 스킵
        if (blockchainProperties.getContractAddress() == null ||
                blockchainProperties.getContractAddress().isEmpty()) {
            log.warn("Blockchain contract address not configured. Skipping sync.");
            return;
        }

        for (LedgerEntry entry : pendingEntries) {
            try {
                String txHash = recordToBlockchain(entry);
                ledgerService.markAsRecorded(entry.getId(), txHash);
                log.info("Entry {} recorded to blockchain with txHash: {}", entry.getId(), txHash);
            } catch (RuntimeException e) {
                // 블록체인 연동 중 발생하는 모든 런타임 예외를 graceful하게 처리
                log.error("Failed to record entry {} to blockchain: {}", entry.getId(), e.getMessage());
                ledgerService.markAsFailed(entry.getId());
            }
        }
    }

    private String recordToBlockchain(LedgerEntry entry) {
        // TODO: 실제 Web3j로 스마트 컨트랙트 호출
        // 현재는 Mock txHash 반환 (Testnet 연결 전)
        return "0x" + entry.getDataHashValue().substring(0, 64);
    }
}
