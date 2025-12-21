package com.vericerti.controller;

import com.vericerti.application.command.CreateLedgerEntryCommand;
import com.vericerti.config.BaseIntegrationTest;
import com.vericerti.controller.ledger.response.LedgerResponse;
import com.vericerti.controller.ledger.response.VerifyResponse;
import com.vericerti.domain.common.vo.BusinessNumber;
import com.vericerti.domain.ledger.entity.LedgerEntityType;
import com.vericerti.domain.ledger.entity.LedgerEntry;
import com.vericerti.domain.ledger.service.LedgerService;
import com.vericerti.domain.organization.entity.Organization;
import com.vericerti.domain.organization.repository.OrganizationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class LedgerControllerIntegrationTest extends BaseIntegrationTest {

    @LocalServerPort
    private int port;

    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private LedgerService ledgerService;

    @Autowired
    private OrganizationRepository organizationRepository;

    private Organization testOrg;

    private String baseUrl() {
        return "http://localhost:" + port;
    }

    @BeforeEach
    void setUp() {
        testOrg = organizationRepository.save(Organization.builder()
                .name("테스트 단체")
                .businessNumber(BusinessNumber.of("BN-" + UUID.randomUUID()))
                .description("테스트용")
                .build());
    }

    @Test
    @DisplayName("GET /api/organizations/{orgId}/ledger - Ledger 목록 조회")
    void getLedgerEntries_shouldReturnList() {
        // given
        ledgerService.createEntry(new CreateLedgerEntryCommand(testOrg.getId(), LedgerEntityType.DONATION, 100L, "content1".getBytes(), "file1.pdf"));
        ledgerService.createEntry(new CreateLedgerEntryCommand(testOrg.getId(), LedgerEntityType.DONATION, 101L, "content2".getBytes(), "file2.pdf"));

        // when
        ResponseEntity<List<LedgerResponse>> response = restTemplate.exchange(
                baseUrl() + "/api/organizations/" + testOrg.getId() + "/ledger",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        // then
        assertAll(
            ()-> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
            ()-> assertThat(response.getBody()).hasSize(2),
            ()->assertThat(response.getBody().get(0).organizationId()).isEqualTo(testOrg.getId())
        );
    }

    @Test
    @DisplayName("GET /api/organizations/{orgId}/ledger - 빈 목록")
    void getLedgerEntries_withNoEntries_shouldReturnEmpty() {
        // given - create new org with no entries
        Organization emptyOrg = organizationRepository.save(Organization.builder()
                .name("빈 단체")
                .businessNumber(BusinessNumber.of("BN-" + UUID.randomUUID()))
                .description("항목 없음")
                .build());

        // when
        ResponseEntity<List<LedgerResponse>> response = restTemplate.exchange(
                baseUrl() + "/api/organizations/" + emptyOrg.getId() + "/ledger",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        // then
        assertAll(
                ()->assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                ()->assertThat(response.getBody()).isEmpty()
        );
    }

    @Test
    @DisplayName("GET /api/ledger/verify/{txHash} - 존재하는 트랜잭션 검증")
    void verifyByTxHash_withValidTx_shouldReturnVerified() {
        // given
        LedgerEntry entry = ledgerService.createEntry(new CreateLedgerEntryCommand(testOrg.getId(), LedgerEntityType.DONATION, 100L, "content".getBytes(), "file.pdf"));
        String txHash = "0x1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef";
        ledgerService.markAsRecorded(entry.getId(), txHash);

        // when
        ResponseEntity<VerifyResponse> response = restTemplate.getForEntity(
                baseUrl() + "/api/ledger/verify/" + txHash,
                VerifyResponse.class
        );

        // then
        assertAll(
                ()->assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                ()->assertThat(response.getBody()).isNotNull(),
                ()->assertThat(response.getBody().verified()).isTrue(),
                ()->assertThat(response.getBody().txHash()).isEqualTo(txHash)
        );
    }

    @Test
    @DisplayName("GET /api/ledger/verify/{txHash} - 존재하지 않는 트랜잭션")
    void verifyByTxHash_withInvalidTx_shouldReturnNotFound() {
        // when
        ResponseEntity<VerifyResponse> response = restTemplate.getForEntity(
                baseUrl() + "/api/ledger/verify/0xnonexistent",
                VerifyResponse.class
        );

        // then
        assertAll(
                ()->assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                ()->assertThat(response.getBody()).isNotNull(),
                ()->assertThat(response.getBody().verified()).isFalse()
        );
    }
}


