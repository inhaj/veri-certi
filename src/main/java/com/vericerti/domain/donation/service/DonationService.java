package com.vericerti.domain.donation.service;

import com.vericerti.application.command.CreateDonationCommand;
import com.vericerti.domain.donation.entity.Donation;
import com.vericerti.domain.donation.repository.DonationRepository;
import com.vericerti.domain.organization.repository.OrganizationRepository;
import com.vericerti.infrastructure.exception.EntityNotFoundException;
import com.vericerti.infrastructure.exception.ErrorCode;
import com.vericerti.infrastructure.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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

        if (command.amount() == null || command.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.INVALID_DONATION_AMOUNT, "Amount must be positive");
        }

        Donation donation = Donation.create(
                command.organizationId(), 
                command.memberId(), 
                command.amount(), 
                command.purpose()
        );
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



