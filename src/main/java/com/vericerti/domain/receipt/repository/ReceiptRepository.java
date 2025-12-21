package com.vericerti.domain.receipt.repository;

import com.vericerti.domain.receipt.entity.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ReceiptRepository extends JpaRepository<Receipt, Long> {
    List<Receipt> findByOrganizationId(Long organizationId);
    List<Receipt> findByAccountId(Long accountId);
    List<Receipt> findByOrganizationIdAndIssueDateBetween(Long organizationId, LocalDate start, LocalDate end);
}
