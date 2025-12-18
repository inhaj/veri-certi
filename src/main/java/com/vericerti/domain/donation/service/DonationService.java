package com.vericerti.domain.donation.service;

import com.vericerti.domain.donation.entity.Donation;
import com.vericerti.domain.donation.repository.DonationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DonationService {

    private final DonationRepository donationRepository;

    @Transactional
    public Donation createDonation(Long organizationId, Long memberId, BigDecimal amount, String purpose) {
        Donation donation = Donation.builder()
                .organizationId(organizationId)
                .memberId(memberId)
                .amount(amount)
                .purpose(purpose)
                .build();

        return donationRepository.save(donation);
    }

    @Transactional(readOnly = true)
    public List<Donation> findByOrganization(Long organizationId) {
        return donationRepository.findByOrganizationIdOrderByDonatedAtDesc(organizationId);
    }

    @Transactional(readOnly = true)
    public Donation findById(Long id) {
        return donationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Donation not found: " + id));
    }
}
