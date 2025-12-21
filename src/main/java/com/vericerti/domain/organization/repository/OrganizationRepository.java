package com.vericerti.domain.organization.repository;

import com.vericerti.domain.organization.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Organization Repository
 */
public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    @Query("SELECT o FROM Organization o WHERE o.businessNumber.value = :businessNumber")
    Optional<Organization> findByBusinessNumber(@Param("businessNumber") String businessNumber);
    
    @Query("SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END FROM Organization o WHERE o.businessNumber.value = :businessNumber")
    boolean existsByBusinessNumber(@Param("businessNumber") String businessNumber);
}



