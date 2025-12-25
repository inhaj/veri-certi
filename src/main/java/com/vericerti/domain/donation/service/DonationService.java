package com.vericerti.domain.donation.service;

import com.vericerti.application.command.CreateDonationCommand;
import com.vericerti.domain.common.vo.Money;
import com.vericerti.domain.donation.entity.Donation;
import com.vericerti.domain.donation.repository.DonationRepository;
import com.vericerti.domain.organization.repository.OrganizationRepository;
import com.vericerti.infrastructure.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DonationService {

    private final DonationRepository donationRepository;
    private final OrganizationRepository organizationRepository;

    @Transactional
    public Donation createDonation(CreateDonationCommand command) {
        if (!organizationRepository.existsById(command.organizationId())) {
            throw EntityNotFoundException.organization(command.organizationId());
        }

        Donation donation = Donation.builder()
                .organizationId(command.organizationId())
                .memberId(command.memberId())
                .amount(Money.of(command.amount()))
                .purpose(command.purpose())
                .build();
        Donation saved = donationRepository.save(donation);
        
        log.info("event=donation_created orgId={} donationId={} amount={}", 
                command.organizationId(), saved.getId(), command.amount());
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Donation> findByOrganization(Long organizationId) {
        if (!organizationRepository.existsById(organizationId)) {
            throw EntityNotFoundException.organization(organizationId);
        }
        return donationRepository.findByOrganizationIdOrderByDonatedAtDesc(organizationId);
    }

    @Transactional(readOnly = true)
    public Donation findById(Long donationId) {
        return donationRepository.findById(donationId)
                .orElseThrow(() -> EntityNotFoundException.donation(donationId));
    }
}



