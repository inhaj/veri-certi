package com.vericerti.domain.donation.repository;

import com.vericerti.domain.donation.entity.Donation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DonationRepository extends JpaRepository<Donation, Long> {
    List<Donation> findByOrganizationIdOrderByDonatedAtDesc(Long organizationId);
    List<Donation> findByMemberIdOrderByDonatedAtDesc(Long memberId);
}
