package com.vericerti.domain.donation.service;

import com.vericerti.application.command.CreateDonationCommand;
import com.vericerti.config.BaseIntegrationTest;
import com.vericerti.domain.common.vo.BusinessNumber;
import com.vericerti.domain.donation.entity.Donation;
import com.vericerti.domain.organization.entity.Organization;
import com.vericerti.domain.organization.repository.OrganizationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class DonationServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private DonationService donationService;

    @Autowired
    private OrganizationRepository organizationRepository;

    private Organization testOrg1;
    private Organization testOrg2;

    @BeforeEach
    void setUp() {
        testOrg1 = organizationRepository.save(Organization.builder()
                .name("테스트 단체 1")
                .businessNumber(BusinessNumber.of("BN-" + UUID.randomUUID()))
                .description("테스트용")
                .build());

        testOrg2 = organizationRepository.save(Organization.builder()
                .name("테스트 단체 2")
                .businessNumber(BusinessNumber.of("BN-" + UUID.randomUUID()))
                .description("다른 단체")
                .build());
    }

    @Test
    @DisplayName("createDonation - 기부 생성 및 DB 저장")
    void createDonation_shouldSaveDonation() {
        // given
        Long memberId = 100L;
        BigDecimal amount = new BigDecimal("50000.00");
        String purpose = "교육 지원";

        // when
        Donation donation = donationService.createDonation(
                new CreateDonationCommand(testOrg1.getId(), memberId, amount, purpose)
        );

        // then
        assertAll(
                () -> assertThat(donation.getId()).isNotNull(),
                () -> assertThat(donation.getOrganizationId()).isEqualTo(testOrg1.getId()),
                () -> assertThat(donation.getMemberId()).isEqualTo(memberId),
                () -> assertThat(donation.getAmountValue().orElse(null)).isEqualByComparingTo(amount),
                () -> assertThat(donation.getPurpose()).isEqualTo(purpose),
                () -> assertThat(donation.getDonatedAt()).isNotNull()
        );
    }

    @Test
    @DisplayName("findByOrganization - 조직별 기부 목록 조회")
    void findByOrganization_shouldReturnDonationsOrderedByDate() {
        // given
        donationService.createDonation(new CreateDonationCommand(testOrg1.getId(), 100L, new BigDecimal("10000"), "기부1"));
        donationService.createDonation(new CreateDonationCommand(testOrg1.getId(), 101L, new BigDecimal("20000"), "기부2"));
        donationService.createDonation(new CreateDonationCommand(testOrg2.getId(), 200L, new BigDecimal("30000"), "다른 조직"));

        // when
        List<Donation> donations = donationService.findByOrganization(testOrg1.getId());

        // then
        assertAll(
                () -> assertThat(donations).hasSize(2),
                () -> assertThat(donations).extracting(Donation::getOrganizationId).containsOnly(testOrg1.getId())
        );
    }

    @Test
    @DisplayName("findById - ID로 기부 조회")
    void findById_shouldReturnDonation() {
        // given
        Donation created = donationService.createDonation(
                new CreateDonationCommand(testOrg1.getId(), 100L, new BigDecimal("50000"), "테스트")
        );

        // when
        Donation found = donationService.findById(created.getId());

        // then
        assertAll(
                () -> assertThat(found.getId()).isEqualTo(created.getId()),
                () -> assertThat(found.getAmountValue().orElse(null)).isEqualByComparingTo(new BigDecimal("50000"))
        );
    }

    @Test
    @DisplayName("findById - 존재하지 않는 ID")
    void findById_withInvalidId_shouldThrow() {
        assertThatThrownBy(() -> donationService.findById(999L))
                .isInstanceOf(com.vericerti.infrastructure.exception.EntityNotFoundException.class);
    }
}



