package com.vericerti.controller;

import com.vericerti.application.command.SignupCommand;
import com.vericerti.application.dto.TokenResult;
import com.vericerti.config.BaseIntegrationTest;
import com.vericerti.controller.account.request.AccountCreateRequest;
import com.vericerti.controller.account.response.AccountResponse;
import com.vericerti.domain.account.entity.Account;
import com.vericerti.domain.account.entity.AccountType;
import com.vericerti.domain.auth.service.AuthService;
import com.vericerti.domain.common.vo.AccountNumber;
import com.vericerti.domain.common.vo.BusinessNumber;
import com.vericerti.domain.member.entity.MemberRole;
import com.vericerti.domain.organization.entity.Organization;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class AccountControllerIntegrationTest extends BaseIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private AuthService authService;

    private final RestTemplate restTemplate = new RestTemplate();
    private Organization testOrg;
    private String accessToken;

    private String baseUrl() {
        return "http://localhost:" + port + "/api/organizations/" + testOrg.getId() + "/accounts";
    }

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 생성 및 로그인
        String email = "test-" + UUID.randomUUID() + "@example.com";
        authService.signup(new SignupCommand(email, "password123", MemberRole.ADMIN));
        TokenResult tokens = authService.login(new com.vericerti.application.command.LoginCommand(email, "password123"));
        accessToken = tokens.accessToken();

        testOrg = organizationRepository.save(Organization.builder()
                .name("테스트 단체")
                .businessNumber(BusinessNumber.of("BN-" + UUID.randomUUID()))
                .description("테스트용")
                .build());
    }

    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);
        return headers;
    }

    @Test
    @DisplayName("POST /api/organizations/{orgId}/accounts - 계좌 생성")
    void create_shouldReturnAccount() {
        // given
        AccountCreateRequest request = new AccountCreateRequest(
                "1234567890",
                "국민은행",
                AccountType.OPERATING,
                "테스트 단체",
                new BigDecimal("1000000.00"),
                "운영계좌"
        );

        HttpEntity<AccountCreateRequest> entity = new HttpEntity<>(request, createAuthHeaders());

        // when
        ResponseEntity<AccountResponse> response = restTemplate.postForEntity(
                baseUrl(),
                entity,
                AccountResponse.class
        );

        // then
        assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED),
                () -> assertThat(response.getBody()).isNotNull(),
                () -> assertThat(response.getBody().accountNumber()).isEqualTo("1234567890"),
                () -> assertThat(response.getBody().bankName()).isEqualTo("국민은행")
        );
    }

    @Test
    @DisplayName("GET /api/organizations/{orgId}/accounts - 계좌 목록")
    void list_shouldReturnAccounts() {
        // given
        accountRepository.save(Account.builder()
                .organizationId(testOrg.getId())
                .accountNumber(AccountNumber.of(String.format("%014d", System.nanoTime() % 10000000000000L)))
                .bankName("국민은행")
                .accountType(AccountType.OPERATING)
                .accountHolder("홀더")
                .build());

        HttpEntity<?> entity = new HttpEntity<>(createAuthHeaders());

        // when
        ResponseEntity<AccountResponse[]> response = restTemplate.exchange(
                baseUrl(),
                HttpMethod.GET,
                entity,
                AccountResponse[].class
        );

        // then
        assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                () -> assertThat(response.getBody()).hasSize(1)
        );
    }

    @Test
    @DisplayName("GET /api/organizations/{orgId}/accounts/{id} - 계좌 조회")
    void get_shouldReturnAccount() {
        // given
        Account account = accountRepository.save(Account.builder()
                .organizationId(testOrg.getId())
                .accountNumber(AccountNumber.of(String.format("%014d", System.nanoTime() % 10000000000000L)))
                .bankName("신한은행")
                .accountType(AccountType.RESERVE)
                .accountHolder("테스트")
                .build());

        HttpEntity<?> entity = new HttpEntity<>(createAuthHeaders());

        // when
        ResponseEntity<AccountResponse> response = restTemplate.exchange(
                baseUrl() + "/" + account.getId(),
                HttpMethod.GET,
                entity,
                AccountResponse.class
        );

        // then
        assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                () -> assertThat(response.getBody()).isNotNull(),
                () -> assertThat(response.getBody().id()).isEqualTo(account.getId())
        );
    }

    @Test
    @DisplayName("DELETE /api/organizations/{orgId}/accounts/{id} - 계좌 삭제")
    void delete_shouldReturn204() {
        // given
        Account account = accountRepository.save(Account.builder()
                .organizationId(testOrg.getId())
                .accountNumber(AccountNumber.of(String.format("%014d", System.nanoTime() % 10000000000000L)))
                .bankName("삭제은행")
                .accountType(AccountType.OTHER)
                .accountHolder("홀더")
                .build());

        HttpEntity<?> entity = new HttpEntity<>(createAuthHeaders());

        // when
        restTemplate.exchange(
                baseUrl() + "/" + account.getId(),
                HttpMethod.DELETE,
                entity,
                Void.class
        );

        // then
        assertThat(accountRepository.existsById(account.getId())).isFalse();
    }
}

