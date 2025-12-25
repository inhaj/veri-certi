package com.vericerti.controller;

import com.vericerti.application.command.CreateReceiptCommand;
import com.vericerti.application.command.SignupCommand;
import com.vericerti.application.dto.TokenResult;
import com.vericerti.config.BaseIntegrationTest;
import com.vericerti.controller.receipt.response.ReceiptResponse;
import com.vericerti.domain.account.entity.Account;
import com.vericerti.domain.account.entity.AccountType;
import com.vericerti.domain.auth.service.AuthService;
import com.vericerti.domain.common.vo.BusinessNumber;
import com.vericerti.domain.member.entity.MemberRole;
import com.vericerti.domain.organization.entity.Organization;
import com.vericerti.domain.receipt.entity.Receipt;
import com.vericerti.domain.receipt.entity.ReceiptCategory;
import com.vericerti.domain.receipt.service.ReceiptService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class ReceiptControllerIntegrationTest extends BaseIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private AuthService authService;

    @Autowired
    private ReceiptService receiptService;

    private final RestTemplate restTemplate = new RestTemplate();
    private Organization testOrg;
    private Account testAccount;
    private String accessToken;

    private String baseUrl() {
        return "http://localhost:" + port + "/api/organizations/" + testOrg.getId() + "/receipts";
    }

    @BeforeEach
    void setUp() {
        // 사용자 생성 및 로그인
        String email = "test-" + UUID.randomUUID() + "@example.com";
        authService.signup(new SignupCommand(email, "password123", MemberRole.ADMIN));
        TokenResult tokens = authService.login(new com.vericerti.application.command.LoginCommand(email, "password123"));
        accessToken = tokens.accessToken();

        // 테스트 Organization 생성
        testOrg = organizationRepository.save(Organization.builder()
                .name("테스트 단체")
                .businessNumber(BusinessNumber.of("BN-" + UUID.randomUUID()))
                .description("테스트용")
                .build());

        // 테스트 Account 생성
        testAccount = accountRepository.save(Account.builder()
                .organizationId(testOrg.getId())
                .accountNumber("ACC-" + UUID.randomUUID())
                .bankName("테스트은행")
                .accountType(AccountType.OPERATING)
                .accountHolder("테스트")
                .build());
    }

    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);
        return headers;
    }

    @Test
    @DisplayName("GET /api/organizations/{orgId}/receipts - 영수증 목록 조회")
    void list_shouldReturnReceipts() {
        // given
        receiptService.createReceipt(new CreateReceiptCommand(
                testOrg.getId(), testAccount.getId(), new BigDecimal("10000"),
                LocalDate.now(), "상점1", null, null, ReceiptCategory.OFFICE, null
        ));
        receiptService.createReceipt(new CreateReceiptCommand(
                testOrg.getId(), testAccount.getId(), new BigDecimal("20000"),
                LocalDate.now(), "상점2", null, null, ReceiptCategory.UTILITIES, null
        ));

        HttpEntity<?> entity = new HttpEntity<>(createAuthHeaders());

        // when
        ResponseEntity<ReceiptResponse[]> response = restTemplate.exchange(
                baseUrl(),
                HttpMethod.GET,
                entity,
                ReceiptResponse[].class
        );

        // then
        assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                () -> assertThat(response.getBody()).hasSize(2)
        );
    }

    @Test
    @DisplayName("GET /api/organizations/{orgId}/receipts/{id} - 개별 영수증 조회")
    void get_shouldReturnReceipt() {
        // given
        Receipt receipt = receiptService.createReceipt(new CreateReceiptCommand(
                testOrg.getId(), null, new BigDecimal("50000"),
                LocalDate.now(), "테스트 상점", null, null, ReceiptCategory.OTHER, "테스트"
        ));

        HttpEntity<?> entity = new HttpEntity<>(createAuthHeaders());

        // when
        ResponseEntity<ReceiptResponse> response = restTemplate.exchange(
                baseUrl() + "/" + receipt.getId(),
                HttpMethod.GET,
                entity,
                ReceiptResponse.class
        );

        // then
        assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                () -> assertThat(response.getBody()).isNotNull(),
                () -> assertThat(response.getBody().id()).isEqualTo(receipt.getId())
        );
    }

    @Test
    @DisplayName("GET /api/organizations/{orgId}/receipts/by-date - 날짜 범위 조회")
    void listByDateRange_shouldReturnReceipts() {
        // given
        LocalDate today = LocalDate.now();
        receiptService.createReceipt(new CreateReceiptCommand(
                testOrg.getId(), null, new BigDecimal("30000"),
                today, "오늘 상점", null, null, ReceiptCategory.OFFICE, null
        ));

        HttpEntity<?> entity = new HttpEntity<>(createAuthHeaders());

        // when
        ResponseEntity<ReceiptResponse[]> response = restTemplate.exchange(
                baseUrl() + "/by-date?start=" + today.minusDays(1) + "&end=" + today.plusDays(1),
                HttpMethod.GET,
                entity,
                ReceiptResponse[].class
        );

        // then
        assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                () -> assertThat(response.getBody()).hasSize(1)
        );
    }

    @Test
    @DisplayName("DELETE /api/organizations/{orgId}/receipts/{id} - 영수증 삭제")
    void delete_shouldReturn204() {
        // given
        Receipt receipt = receiptService.createReceipt(new CreateReceiptCommand(
                testOrg.getId(), null, new BigDecimal("5000"),
                LocalDate.now(), "삭제 테스트", null, null, ReceiptCategory.OTHER, null
        ));

        HttpEntity<?> entity = new HttpEntity<>(createAuthHeaders());

        // when
        restTemplate.exchange(
                baseUrl() + "/" + receipt.getId(),
                HttpMethod.DELETE,
                entity,
                Void.class
        );

        // then
        assertThat(receiptRepository.existsById(receipt.getId())).isFalse();
    }
}
