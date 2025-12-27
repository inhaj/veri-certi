package com.vericerti.infrastructure.blockchain;

import com.vericerti.config.BaseIntegrationTest;
import com.vericerti.domain.ledger.entity.LedgerEntry;
import com.vericerti.domain.ledger.entity.LedgerEntityType;
import com.vericerti.domain.ledger.entity.LedgerStatus;
import com.vericerti.domain.ledger.repository.LedgerEntryRepository;
import com.vericerti.domain.ledger.service.LedgerService;
import com.vericerti.domain.organization.entity.Organization;
import com.vericerti.domain.organization.repository.OrganizationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class BlockchainIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private LedgerService ledgerService;

    @Autowired
    private LedgerEntryRepository ledgerEntryRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private Web3jService web3jService;

    @Autowired
    private LedgerRegistryService ledgerRegistryService;

    @Autowired
    private BlockchainSyncScheduler blockchainSyncScheduler;

    @Autowired
    private BlockchainSubmitScheduler blockchainSubmitScheduler;

    private Organization testOrganization;

    @BeforeEach
    void setUp() {
        testOrganization = organizationRepository.save(
                Organization.builder()
                        .name("Test Organization")
                        .businessNumber(com.vericerti.domain.common.vo.BusinessNumber.of("123-45-67890"))
                        .build()
        );
    }

    @Test
    @DisplayName("PENDING 상태의 LedgerEntry가 블록체인에 기록되는지 확인")
    void shouldRecordPendingEntryToBlockchain() {
        if (!web3jService.isInitialized()) {
            System.out.println("⚠️ Web3j not initialized. Skipping blockchain test.");
            return;
        }

        String testHash = "a".repeat(64);
        LedgerEntry entry = LedgerEntry.builder()
                .organizationId(testOrganization.getId())
                .entityType(LedgerEntityType.DONATION)
                .entityId(1L)
                .dataHash(com.vericerti.domain.common.vo.DataHash.of(testHash))
                .build();
        
        LedgerEntry savedEntry = ledgerEntryRepository.save(entry);
        assertThat(savedEntry.getStatus()).isEqualTo(LedgerStatus.PENDING);

        blockchainSubmitScheduler.submitPendingToBlockchain();

        LedgerEntry updatedEntry = ledgerEntryRepository.findById(savedEntry.getId()).orElseThrow();
        
        assertThat(updatedEntry.getStatus()).isEqualTo(LedgerStatus.RECORDED);
        assertThat(updatedEntry.getBlockchainTxHash()).isNotNull();
        assertThat(updatedEntry.getBlockchainTxHash()).startsWith("0x");

        System.out.println("✅ Entry recorded to blockchain!");
        System.out.println("   TxHash: " + updatedEntry.getBlockchainTxHash());
    }

    @Test
    @DisplayName("블록체인에서 해시 검증이 가능한지 확인")
    void shouldVerifyHashOnBlockchain() {
        if (!web3jService.isInitialized()) {
            System.out.println("⚠️ Web3j not initialized. Skipping blockchain test.");
            return;
        }

        String contractAddress = web3jService.getContractAddress();
        if (contractAddress == null || contractAddress.isBlank()) {
            System.out.println("⚠️ Contract address not configured. Skipping blockchain test.");
            return;
        }

        String testHash = "0x" + "b".repeat(64);
        String txHash = ledgerRegistryService.registerHash(testHash, testOrganization.getId());
        
        assertThat(txHash).isNotNull();
        assertThat(txHash).startsWith("0x");

        LedgerRegistryService.VerificationResult result = ledgerRegistryService.verifyHash(testHash);

        assertThat(result.exists()).isTrue();
        assertThat(result.timestamp()).isGreaterThan(0);

        System.out.println("✅ Hash verification successful!");
    }

    @Test
    @DisplayName("LedgerService를 통한 전체 플로우 테스트")
    void shouldCompleteFullFlow() {
        if (!web3jService.isInitialized()) {
            System.out.println("⚠️ Web3j not initialized. Skipping full flow test.");
            return;
        }

        for (int i = 0; i < 3; i++) {
            String hash = String.format("%064x", i + 100);
            LedgerEntry entry = LedgerEntry.builder()
                    .organizationId(testOrganization.getId())
                    .entityType(LedgerEntityType.RECEIPT)
                    .entityId((long) (i + 1))
                    .dataHash(com.vericerti.domain.common.vo.DataHash.of(hash))
                    .build();
            ledgerEntryRepository.save(entry);
        }

        List<LedgerEntry> pendingBefore = ledgerService.findPendingEntries();
        assertThat(pendingBefore).hasSizeGreaterThanOrEqualTo(3);

        blockchainSubmitScheduler.submitPendingToBlockchain();

        List<LedgerEntry> allEntries = ledgerService.findByOrganization(testOrganization.getId());
        long recordedCount = allEntries.stream()
                .filter(e -> e.getStatus() == LedgerStatus.RECORDED)
                .count();

        System.out.println("✅ Full flow completed!");
        System.out.println("   Total entries: " + allEntries.size());
        System.out.println("   Recorded: " + recordedCount);

        assertThat(recordedCount).isGreaterThan(0);
    }
}
