package com.vericerti.domain.organization.service;

import com.vericerti.application.command.CreateOrganizationCommand;
import com.vericerti.config.BaseIntegrationTest;
import com.vericerti.domain.organization.entity.Organization;
import com.vericerti.domain.organization.repository.OrganizationRepository;
import com.vericerti.infrastructure.exception.DuplicateException;
import com.vericerti.infrastructure.exception.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class OrganizationServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private OrganizationRepository organizationRepository;

    @BeforeEach
    void setUp() {
        // BaseIntegrationTest에서 DB 정리됨
    }

    @Test
    @DisplayName("create - 조직 생성 성공")
    void create_shouldSaveOrganization() {
        // given
        String businessNumber = "BN-" + UUID.randomUUID();
        CreateOrganizationCommand command = new CreateOrganizationCommand(
                "테스트 단체",
                businessNumber,
                "테스트 설명"
        );

        // when
        Organization organization = organizationService.create(command);

        // then
        assertAll(
                () -> assertThat(organization.getId()).isNotNull(),
                () -> assertThat(organization.getName()).isEqualTo("테스트 단체"),
                () -> assertThat(organization.getBusinessNumberValue()).isEqualTo(businessNumber),
                () -> assertThat(organization.getDescription()).isEqualTo("테스트 설명"),
                () -> assertThat(organization.getCreatedAt()).isNotNull()
        );
    }

    @Test
    @DisplayName("create - 중복 사업자번호 예외")
    void create_withDuplicateBusinessNumber_shouldThrow() {
        // given
        String businessNumber = "DUP-BN-001";
        organizationService.create(new CreateOrganizationCommand("단체1", businessNumber, "설명1"));

        // when & then
        assertThatThrownBy(() -> organizationService.create(
                new CreateOrganizationCommand("단체2", businessNumber, "설명2")
        )).isInstanceOf(DuplicateException.class);
    }

    @Test
    @DisplayName("findById - ID로 조직 조회")
    void findById_shouldReturnOrganization() {
        // given
        Organization created = organizationService.create(new CreateOrganizationCommand(
                "조회 테스트", "BN-" + UUID.randomUUID(), "설명"
        ));

        // when
        Organization found = organizationService.findById(created.getId());

        // then
        assertAll(
                () -> assertThat(found.getId()).isEqualTo(created.getId()),
                () -> assertThat(found.getName()).isEqualTo("조회 테스트")
        );
    }

    @Test
    @DisplayName("findById - 존재하지 않는 ID")
    void findById_withInvalidId_shouldThrow() {
        assertThatThrownBy(() -> organizationService.findById(999L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("findByBusinessNumber - 사업자번호로 조직 조회")
    void findByBusinessNumber_shouldReturnOrganization() {
        // given
        String businessNumber = "UNIQUE-BN-" + UUID.randomUUID();
        organizationService.create(new CreateOrganizationCommand(
                "사업자번호 조회", businessNumber, "설명"
        ));

        // when
        Organization found = organizationService.findByBusinessNumber(businessNumber);

        // then
        assertThat(found.getBusinessNumberValue()).isEqualTo(businessNumber);
    }

    @Test
    @DisplayName("findByBusinessNumber - 존재하지 않는 사업자번호")
    void findByBusinessNumber_withInvalidNumber_shouldThrow() {
        assertThatThrownBy(() -> organizationService.findByBusinessNumber("NONEXISTENT-001"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("findAll - 모든 조직 조회")
    void findAll_shouldReturnAllOrganizations() {
        // given
        organizationService.create(new CreateOrganizationCommand("단체A", "BN-A-" + UUID.randomUUID(), null));
        organizationService.create(new CreateOrganizationCommand("단체B", "BN-B-" + UUID.randomUUID(), null));
        organizationService.create(new CreateOrganizationCommand("단체C", "BN-C-" + UUID.randomUUID(), null));

        // when
        List<Organization> organizations = organizationService.findAll();

        // then
        assertThat(organizations).hasSize(3);
    }

    @Test
    @DisplayName("delete - 조직 삭제")
    void delete_shouldRemoveOrganization() {
        // given
        Organization created = organizationService.create(new CreateOrganizationCommand(
                "삭제 대상", "DEL-" + UUID.randomUUID(), null
        ));
        Long orgId = created.getId();

        // when
        organizationService.delete(orgId);

        // then
        assertThat(organizationRepository.existsById(orgId)).isFalse();
    }

    @Test
    @DisplayName("delete - 존재하지 않는 조직 삭제")
    void delete_withInvalidId_shouldThrow() {
        assertThatThrownBy(() -> organizationService.delete(999L))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
