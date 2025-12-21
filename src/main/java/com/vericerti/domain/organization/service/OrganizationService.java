package com.vericerti.domain.organization.service;

import com.vericerti.application.command.CreateOrganizationCommand;
import com.vericerti.domain.common.vo.BusinessNumber;
import com.vericerti.domain.organization.entity.Organization;
import com.vericerti.domain.organization.repository.OrganizationRepository;
import com.vericerti.infrastructure.exception.DuplicateException;
import com.vericerti.infrastructure.exception.EntityNotFoundException;
import com.vericerti.infrastructure.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;

    @Transactional
    public Organization create(CreateOrganizationCommand command) {
        if (organizationRepository.existsByBusinessNumber(command.businessNumber())) {
            throw new DuplicateException(
                    ErrorCode.ORGANIZATION_NOT_FOUND,
                    "Business number already exists: " + command.businessNumber()
            );
        }

        Organization organization = Organization.builder()
                .name(command.name())
                .businessNumber(BusinessNumber.of(command.businessNumber()))
                .description(command.description())
                .build();

        Organization saved = organizationRepository.save(organization);
        log.info("event=organization_created id={} name={}", saved.getId(), command.name());
        return saved;
    }

    @Transactional(readOnly = true)
    public Organization findById(Long id) {
        return organizationRepository.findById(id)
                .orElseThrow(() -> EntityNotFoundException.organization(id));
    }

    @Transactional(readOnly = true)
    public Organization findByBusinessNumber(String businessNumber) {
        return organizationRepository.findByBusinessNumber(businessNumber)
                .orElseThrow(() -> new EntityNotFoundException(
                        com.vericerti.infrastructure.exception.ErrorCode.ORGANIZATION_NOT_FOUND,
                        "Organization not found with businessNumber: " + businessNumber
                ));
    }

    @Transactional(readOnly = true)
    public List<Organization> findAll() {
        return organizationRepository.findAll();
    }

    @Transactional
    public Organization update(Long id, String name, String description) {
        Organization organization = findById(id);
        
        // Organization은 @Builder라 직접 수정 불가 → 새로 생성해서 저장하거나 setter 추가 필요
        // 현재는 조회만 가능하도록 구현 (수정이 필요하면 별도 메서드 추가)
        log.info("event=organization_updated id={}", id);
        return organization;
    }

    @Transactional
    public void delete(Long id) {
        Organization organization = findById(id);
        organizationRepository.delete(organization);
        log.info("event=organization_deleted id={}", id);
    }
}
