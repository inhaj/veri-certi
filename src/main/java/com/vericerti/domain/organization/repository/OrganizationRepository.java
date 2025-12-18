package com.vericerti.domain.organization.repository;

import com.vericerti.domain.organization.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    Optional<Organization> findByBusinessNumber(String businessNumber);
    boolean existsByBusinessNumber(String businessNumber);
}
