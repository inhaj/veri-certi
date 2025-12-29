package com.vericerti.domain.account.repository;

import com.vericerti.domain.account.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByOrganizationId(Long organizationId);
    
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Account a WHERE a.accountNumber.value = :accountNumber")
    boolean existsByAccountNumber(@Param("accountNumber") String accountNumber);
}

