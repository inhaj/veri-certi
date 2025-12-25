package com.vericerti.controller;

import com.vericerti.application.command.CreateDonationCommand;
import com.vericerti.application.command.SignupCommand;
import com.vericerti.application.dto.TokenResult;
import com.vericerti.config.BaseIntegrationTest;
import com.vericerti.controller.donation.response.DonationResponse;
import com.vericerti.domain.auth.service.AuthService;
import com.vericerti.domain.common.vo.BusinessNumber;
import com.vericerti.domain.donation.entity.Donation;
import com.vericerti.domain.donation.service.DonationService;
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

class DonationControllerIntegrationTest extends BaseIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private AuthService authService;

    @Autowired
    private DonationService donationService;

    private final RestTemplate restTemplate = new RestTemplate();
    private Organization testOrg;
    private String accessToken;

    private String baseUrl() {
        return "http://localhost:" + port + "/api/organizations/" + testOrg.getId() + "/donations";
    }

    @BeforeEach
    void setUp() {
        // 사용자 생성 및 로그인
        String email = "test-" + UUID.randomUUID() + "@example.com";
        authService.signup(new SignupCommand(email, "password123", MemberRole.DONOR));
        TokenResult tokens = authService.login(new com.vericerti.application.command.LoginCommand(email, "password123"));
        accessToken = tokens.accessToken();

        // 테스트 Organization 생성
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
    @DisplayName("GET /api/organizations/{orgId}/donations - 기부 목록 조회")
    void getDonations_shouldReturnList() {
        // given - 기부 데이터 직접 생성
        donationService.createDonation(new CreateDonationCommand(
                testOrg.getId(), 1L, new BigDecimal("10000"), "교육 지원"
        ));
        donationService.createDonation(new CreateDonationCommand(
                testOrg.getId(), 2L, new BigDecimal("20000"), "의료 지원"
        ));

        HttpEntity<?> entity = new HttpEntity<>(createAuthHeaders());

        // when
        ResponseEntity<DonationResponse[]> response = restTemplate.exchange(
                baseUrl(),
                HttpMethod.GET,
                entity,
                DonationResponse[].class
        );

        // then
        assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                () -> assertThat(response.getBody()).hasSize(2)
        );
    }

    @Test
    @DisplayName("GET /api/organizations/{orgId}/donations/{id} - 개별 기부 조회")
    void getDonation_shouldReturnDonation() {
        // given
        Donation donation = donationService.createDonation(new CreateDonationCommand(
                testOrg.getId(), 1L, new BigDecimal("50000"), "테스트 기부"
        ));

        HttpEntity<?> entity = new HttpEntity<>(createAuthHeaders());

        // when
        ResponseEntity<DonationResponse> response = restTemplate.exchange(
                baseUrl() + "/" + donation.getId(),
                HttpMethod.GET,
                entity,
                DonationResponse.class
        );

        // then
        assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                () -> assertThat(response.getBody()).isNotNull(),
                () -> assertThat(response.getBody().id()).isEqualTo(donation.getId()),
                () -> assertThat(response.getBody().amount()).isEqualByComparingTo(new BigDecimal("50000"))
        );
    }
}
