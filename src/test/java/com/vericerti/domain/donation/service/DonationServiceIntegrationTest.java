package com.vericerti.domain.donation.service;

import com.vericerti.config.BaseIntegrationTest;
import com.vericerti.domain.donation.entity.Donation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DonationServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private DonationService donationService;


    @Test
    @DisplayName("createDonation - 기부 생성 및 DB 저장")
    void createDonation_shouldSaveDonation() {
        // given
        Long organizationId = 1L;
        Long memberId = 100L;
        BigDecimal amount = new BigDecimal("50000.00");
        String purpose = "교육 지원";

        // when
        Donation donation = donationService.createDonation(organizationId, memberId, amount, purpose);

        // then
        assertThat(donation.getId()).isNotNull();
        assertThat(donation.getOrganizationId()).isEqualTo(organizationId);
        assertThat(donation.getMemberId()).isEqualTo(memberId);
        assertThat(donation.getAmount()).isEqualByComparingTo(amount);
        assertThat(donation.getPurpose()).isEqualTo(purpose);
        assertThat(donation.getDonatedAt()).isNotNull();
    }

    @Test
    @DisplayName("findByOrganization - 조직별 기부 목록 조회")
    void findByOrganization_shouldReturnDonationsOrderedByDate() {
        // given
        Long orgId = 1L;
        Donation d1 = donationService.createDonation(orgId, 100L, new BigDecimal("10000"), "기부1");
        Donation d2 = donationService.createDonation(orgId, 101L, new BigDecimal("20000"), "기부2");
        donationService.createDonation(2L, 200L, new BigDecimal("30000"), "다른 조직"); // 다른 조직

        // when
        List<Donation> donations = donationService.findByOrganization(orgId);

        // then
        assertThat(donations).hasSize(2);
        assertThat(donations).extracting(Donation::getOrganizationId)
                .containsOnly(orgId);
    }

    @Test
    @DisplayName("findById - ID로 기부 조회")
    void findById_shouldReturnDonation() {
        // given
        Donation created = donationService.createDonation(1L, 100L, new BigDecimal("50000"), "테스트");

        // when
        Donation found = donationService.findById(created.getId());

        // then
        assertThat(found.getId()).isEqualTo(created.getId());
        assertThat(found.getAmount()).isEqualByComparingTo(new BigDecimal("50000"));
    }

    @Test
    @DisplayName("findById - 존재하지 않는 ID")
    void findById_withInvalidId_shouldThrow() {
        // when & then
        assertThatThrownBy(() -> donationService.findById(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Donation not found");
    }
}
