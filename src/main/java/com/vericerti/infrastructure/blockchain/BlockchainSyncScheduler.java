package com.vericerti.infrastructure.blockchain;

import com.vericerti.domain.ledger.entity.LedgerEntry;
import com.vericerti.domain.ledger.entity.LedgerStatus;
import com.vericerti.domain.ledger.repository.LedgerEntryRepository;
import com.vericerti.domain.ledger.service.LedgerService;
import com.vericerti.infrastructure.exception.BlockchainException;
import com.vericerti.infrastructure.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;


@Slf4j
@Service
@RequiredArgsConstructor
public class BlockchainSyncScheduler {

    private static final String PENDING_VERIFICATION_KEY = "blockchain:pending:verification";

    private final StringRedisTemplate redisTemplate;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final LedgerRegistryService ledgerRegistryService;
    private final LedgerService ledgerService;
    private final Web3jService web3jService;

    /**
     * 5분마다 SET 조회 → 블록체인 검증 → DB 동기화
     */
    @Scheduled(fixedDelay = 300000) // 5분
    @Transactional
    public void syncVerifiedEntries() {
        if (!web3jService.isInitialized()) {
            return;
        }

        Set<String> members = redisTemplate.opsForSet().members(PENDING_VERIFICATION_KEY);
        if (members == null || members.isEmpty()) {
            return;
        }

        log.info("Syncing {} pending verification entries", members.size());

        for (String member : members) {
            processMember(member);
        }
    }

    private void processMember(String member) {
        try {
            // member 형식: "entryId:txHash:retryCount"
            String[] parts = member.split(":");
            if (parts.length < 3) {
                log.error("Invalid member format: {}", member);
                redisTemplate.opsForSet().remove(PENDING_VERIFICATION_KEY, member);
                return;
            }

            Long entryId = Long.parseLong(parts[0]);
            String txHash = parts[1];
            int retryCount = Integer.parseInt(parts[2]);

            // 블록체인에서 검증
            boolean verified = verifyOnBlockchain(entryId);

            if (verified) {
                // 검증 성공 → DB에 저장 + SET에서 제거
                ledgerService.markAsRecorded(entryId, txHash);
                redisTemplate.opsForSet().remove(PENDING_VERIFICATION_KEY, member);
                log.info("Entry {} verified and recorded. TxHash: {}", entryId, txHash);
            } else {
                // 검증 실패 → retry 처리
                handleVerificationFailure(entryId, txHash, retryCount, member);
            }
        } catch (Exception e) {
            log.error("Failed to process member {}: {}", member, e.getMessage());
        }
    }

    private void handleVerificationFailure(Long entryId, String txHash, int retryCount, String oldMember) {
        // 기존 member 제거
        redisTemplate.opsForSet().remove(PENDING_VERIFICATION_KEY, oldMember);

        if (retryCount == 0) {
            // 첫 실패: 조용히 재시도 예약
            String newMember = entryId + ":" + txHash + ":1";
            redisTemplate.opsForSet().add(PENDING_VERIFICATION_KEY, newMember);
            log.debug("Entry {} verification failed, retry scheduled (attempt 1)", entryId);

        } else if (retryCount == 1) {
            // 두 번째 실패: WARNING
            String newMember = entryId + ":" + txHash + ":2";
            redisTemplate.opsForSet().add(PENDING_VERIFICATION_KEY, newMember);
            log.warn("[WARNING] Entry {} verification failed twice, retry scheduled (attempt 2)", entryId);

        } else {
            // 세 번째 실패: CRITICAL + 포기
            log.error("[CRITICAL] Entry {} verification failed 3 times, marking as FAILED", entryId);
            ledgerService.markAsFailed(entryId);
        }
    }

    private boolean verifyOnBlockchain(Long entryId) {
        LedgerEntry entry = ledgerEntryRepository.findById(entryId)
                .orElseThrow(() -> new IllegalArgumentException("Entry not found: " + entryId));

        String dataHash = entry.getDataHashValue().orElseThrow();
        if (!dataHash.startsWith("0x")) {
            dataHash = "0x" + dataHash;
        }

        LedgerRegistryService.VerificationResult result = ledgerRegistryService.verifyHash(dataHash);
        return result.exists();
    }

    /**
     * 하루 2번 전체 검증 (06:00, 18:00) - 안전망
     */
    @Scheduled(cron = "0 0 6,18 * * *")
    @Transactional
    public void dailyFullVerification() {
        if (!web3jService.isInitialized()) {
            log.debug("Web3j not initialized. Skipping daily verification.");
            return;
        }

        log.info("Starting daily full verification...");

        List<LedgerEntry> recordedEntries = ledgerEntryRepository.findByStatus(LedgerStatus.RECORDED);
        int verified = 0;
        int failed = 0;

        for (LedgerEntry entry : recordedEntries) {
            try {
                boolean isValid = verifyOnBlockchain(entry.getId());
                if (isValid) {
                    verified++;
                } else {
                    log.warn("Entry {} hash not found on blockchain!", entry.getId());
                    failed++;
                }
            } catch (Exception e) {
                log.error("Failed to verify entry {}: {}", entry.getId(), e.getMessage());
                failed++;
            }
        }

        log.info("Daily verification complete: {} verified, {} failed", verified, failed);
    }

    /**
     * Admin manual full sync
     */
    @Transactional
    public SyncResult syncAll() {
        if (!web3jService.isInitialized()) {
            throw new BlockchainException(ErrorCode.BLOCKCHAIN_NOT_INITIALIZED);
        }

        List<LedgerEntry> recordedEntries = ledgerEntryRepository.findByStatus(LedgerStatus.RECORDED);
        int verified = 0;
        int failed = 0;

        for (LedgerEntry entry : recordedEntries) {
            try {
                boolean isValid = verifyOnBlockchain(entry.getId());
                if (isValid) verified++;
                else failed++;
            } catch (Exception e) {
                log.error("Sync failed for entry {}: {}", entry.getId(), e.getMessage());
                failed++;
            }
        }

        return new SyncResult(recordedEntries.size(), verified, failed);
    }

    /**
     * Admin manual single entry sync
     */
    @Transactional
    public boolean syncEntry(Long entryId) {
        if (!web3jService.isInitialized()) {
            throw new BlockchainException(ErrorCode.BLOCKCHAIN_NOT_INITIALIZED);
        }

        return verifyOnBlockchain(entryId);
    }

    public record SyncResult(int total, int verified, int failed) {}
}
