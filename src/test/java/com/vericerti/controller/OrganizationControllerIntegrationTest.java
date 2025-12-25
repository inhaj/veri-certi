package com.vericerti.controller;

import com.vericerti.application.command.SignupCommand;
import com.vericerti.application.dto.TokenResult;
import com.vericerti.config.BaseIntegrationTest;
import com.vericerti.controller.organization.request.OrganizationCreateRequest;
import com.vericerti.controller.organization.response.OrganizationResponse;
import com.vericerti.domain.auth.service.AuthService;
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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class OrganizationControllerIntegrationTest extends BaseIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private AuthService authService;

    private final RestTemplate restTemplate = new RestTemplate();
    private String accessToken;

    private String baseUrl() {
        return "http://localhost:" + port + "/api/organizations";
    }

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 생성 및 로그인
        String email = "test-" + UUID.randomUUID() + "@example.com";
        authService.signup(new SignupCommand(email, "password123", MemberRole.ADMIN));
        TokenResult tokens = authService.login(new com.vericerti.application.command.LoginCommand(email, "password123"));
        accessToken = tokens.accessToken();
    }

    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);
        return headers;
    }

    @Test
    @DisplayName("POST /api/organizations - 조직 생성")
    void create_shouldReturnOrganization() {
        // given
        String businessNumber = "BN-" + UUID.randomUUID();
        OrganizationCreateRequest request = new OrganizationCreateRequest(
                "테스트 단체",
                businessNumber,
                "테스트 설명"
        );

        HttpEntity<OrganizationCreateRequest> entity = new HttpEntity<>(request, createAuthHeaders());

        // when
        ResponseEntity<OrganizationResponse> response = restTemplate.postForEntity(
                baseUrl(),
                entity,
                OrganizationResponse.class
        );

        // then
        assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                () -> assertThat(response.getBody()).isNotNull(),
                () -> assertThat(response.getBody().name()).isEqualTo("테스트 단체"),
                () -> assertThat(response.getBody().businessNumber()).isEqualTo(businessNumber)
        );
    }

    @Test
    @DisplayName("GET /api/organizations/{id} - 조직 조회")
    void getById_shouldReturnOrganization() {
        // given
        Organization org = organizationRepository.save(Organization.builder()
                .name("조회 테스트")
                .businessNumber(BusinessNumber.of("BN-" + UUID.randomUUID()))
                .description("설명")
                .build());

        HttpEntity<?> entity = new HttpEntity<>(createAuthHeaders());

        // when
        ResponseEntity<OrganizationResponse> response = restTemplate.exchange(
                baseUrl() + "/" + org.getId(),
                HttpMethod.GET,
                entity,
                OrganizationResponse.class
        );

        // then
        assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                () -> assertThat(response.getBody()).isNotNull(),
                () -> assertThat(response.getBody().id()).isEqualTo(org.getId())
        );
    }

    @Test
    @DisplayName("GET /api/organizations - 전체 조직 목록")
    void getAll_shouldReturnList() {
        // given
        organizationRepository.save(Organization.builder()
                .name("단체1")
                .businessNumber(BusinessNumber.of("BN-1-" + UUID.randomUUID()))
                .build());
        organizationRepository.save(Organization.builder()
                .name("단체2")
                .businessNumber(BusinessNumber.of("BN-2-" + UUID.randomUUID()))
                .build());

        HttpEntity<?> entity = new HttpEntity<>(createAuthHeaders());

        // when
        ResponseEntity<OrganizationResponse[]> response = restTemplate.exchange(
                baseUrl(),
                HttpMethod.GET,
                entity,
                OrganizationResponse[].class
        );

        // then
        assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                () -> assertThat(response.getBody()).hasSize(2)
        );
    }

    @Test
    @DisplayName("DELETE /api/organizations/{id} - 조직 삭제")
    void delete_shouldReturn204() {
        // given
        Organization org = organizationRepository.save(Organization.builder()
                .name("삭제 대상")
                .businessNumber(BusinessNumber.of("DEL-" + UUID.randomUUID()))
                .build());

        HttpEntity<?> entity = new HttpEntity<>(createAuthHeaders());

        // when
        restTemplate.exchange(
                baseUrl() + "/" + org.getId(),
                HttpMethod.DELETE,
                entity,
                Void.class
        );

        // then
        assertThat(organizationRepository.existsById(org.getId())).isFalse();
    }
}

