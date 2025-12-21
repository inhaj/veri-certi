package com.vericerti.domain.account.repository;

import com.vericerti.domain.account.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByOrganizationId(Long organizationId);
    boolean existsByAccountNumber(String accountNumber);
}
