package com.vericerti.domain.account.service;

import com.vericerti.application.command.CreateAccountCommand;
import com.vericerti.domain.account.entity.Account;
import com.vericerti.domain.account.repository.AccountRepository;
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
public class AccountService {

    private final AccountRepository accountRepository;
    private final OrganizationRepository organizationRepository;

    @Transactional
    public Account createAccount(CreateAccountCommand command) {
        // 단체 존재 확인
        if (!organizationRepository.existsById(command.organizationId())) {
            throw EntityNotFoundException.organization(command.organizationId());
        }

        // 계좌번호 중복 확인
        if (accountRepository.existsByAccountNumber(command.accountNumber())) {
            throw new DuplicateException(
                    ErrorCode.DUPLICATE_ENTRY,
                    "Account number already exists: " + command.accountNumber()
            );
        }

        Account account = Account.builder()
                .organizationId(command.organizationId())
                .accountNumber(command.accountNumber())
                .bankName(command.bankName())
                .accountType(command.accountType())
                .accountHolder(command.accountHolder())
                .balance(command.balance())
                .description(command.description())
                .build();

        Account saved = accountRepository.save(account);
        log.info("event=account_created orgId={} accountId={}", command.organizationId(), saved.getId());
        return saved;
    }

    @Transactional(readOnly = true)
    public Account findById(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> EntityNotFoundException.account(id));
    }

    @Transactional(readOnly = true)
    public List<Account> findByOrganizationId(Long organizationId) {
        return accountRepository.findByOrganizationId(organizationId);
    }

    @Transactional
    public void delete(Long id) {
        if (!accountRepository.existsById(id)) {
            throw EntityNotFoundException.account(id);
        }
        accountRepository.deleteById(id);
        log.info("event=account_deleted accountId={}", id);
    }
}
