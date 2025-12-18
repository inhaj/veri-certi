package com.vericerti.controller;

import com.vericerti.config.BaseIntegrationTest;
import com.vericerti.controller.ledger.response.LedgerResponse;
import com.vericerti.controller.ledger.response.VerifyResponse;
import com.vericerti.domain.ledger.entity.LedgerEntityType;
import com.vericerti.domain.ledger.entity.LedgerEntry;
import com.vericerti.domain.ledger.service.LedgerService;
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

import static org.assertj.core.api.Assertions.assertThat;

class LedgerControllerIntegrationTest extends BaseIntegrationTest {

    @LocalServerPort
    private int port;

    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private LedgerService ledgerService;

    private String baseUrl() {
        return "http://localhost:" + port;
    }

    @Test
    @DisplayName("GET /api/organizations/{orgId}/ledger - Ledger 목록 조회")
    void getLedgerEntries_shouldReturnList() {
        // given
        Long orgId = 1L;
        ledgerService.createEntry(orgId, LedgerEntityType.DONATION, 100L, "content1".getBytes(), "file1.pdf");
        ledgerService.createEntry(orgId, LedgerEntityType.DONATION, 101L, "content2".getBytes(), "file2.pdf");

        // when
        ResponseEntity<List<LedgerResponse>> response = restTemplate.exchange(
                baseUrl() + "/api/organizations/" + orgId + "/ledger",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody().get(0).organizationId()).isEqualTo(orgId);
    }

    @Test
    @DisplayName("GET /api/organizations/{orgId}/ledger - 빈 목록")
    void getLedgerEntries_withNoEntries_shouldReturnEmpty() {
        // when
        ResponseEntity<List<LedgerResponse>> response = restTemplate.exchange(
                baseUrl() + "/api/organizations/999/ledger",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    @DisplayName("GET /api/ledger/verify/{txHash} - 존재하는 트랜잭션 검증")
    void verifyByTxHash_withValidTx_shouldReturnVerified() {
        // given
        LedgerEntry entry = ledgerService.createEntry(1L, LedgerEntityType.DONATION, 100L, "content".getBytes(), "file.pdf");
        String txHash = "0x1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef";
        ledgerService.markAsRecorded(entry.getId(), txHash);

        // when
        ResponseEntity<VerifyResponse> response = restTemplate.getForEntity(
                baseUrl() + "/api/ledger/verify/" + txHash,
                VerifyResponse.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().verified()).isTrue();
        assertThat(response.getBody().txHash()).isEqualTo(txHash);
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
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().verified()).isFalse();
    }
}

