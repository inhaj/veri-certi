package com.vericerti.domain.account.service;

import com.vericerti.application.command.CreateAccountCommand;
import com.vericerti.config.BaseIntegrationTest;
import com.vericerti.domain.account.entity.Account;
import com.vericerti.domain.account.entity.AccountType;
import com.vericerti.domain.account.repository.AccountRepository;
import com.vericerti.domain.common.vo.BusinessNumber;
import com.vericerti.domain.organization.entity.Organization;
import com.vericerti.domain.organization.repository.OrganizationRepository;
import com.vericerti.infrastructure.exception.DuplicateException;
import com.vericerti.infrastructure.exception.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class AccountServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    private Organization testOrg;

    @BeforeEach
    void setUp() {
        testOrg = organizationRepository.save(Organization.builder()
                .name("테스트 단체")
                .businessNumber(BusinessNumber.of("BN-" + UUID.randomUUID()))
                .description("테스트용")
                .build());
    }

    @Test
    @DisplayName("createAccount - 계좌 생성 성공")
    void createAccount_shouldSaveAccount() {
        // given
        CreateAccountCommand command = new CreateAccountCommand(
                testOrg.getId(),
                "123-456-789",
                "국민은행",
                AccountType.OPERATING,
                "테스트 단체",
                new BigDecimal("1000000.00"),
                "운영계좌"
        );

        // when
        Account account = accountService.createAccount(command);

        // then
        assertAll(
                () -> assertThat(account.getId()).isNotNull(),
                () -> assertThat(account.getOrganizationId()).isEqualTo(testOrg.getId()),
                () -> assertThat(account.getAccountNumber()).isEqualTo("123-456-789"),
                () -> assertThat(account.getBankName()).isEqualTo("국민은행"),
                () -> assertThat(account.getAccountType()).isEqualTo(AccountType.OPERATING),
                () -> assertThat(account.getBalance()).isEqualByComparingTo(new BigDecimal("1000000.00")),
                () -> assertThat(account.getCreatedAt()).isNotNull()
        );
    }

    @Test
    @DisplayName("createAccount - 존재하지 않는 조직")
    void createAccount_withInvalidOrg_shouldThrow() {
        // given
        CreateAccountCommand command = new CreateAccountCommand(
                999L,
                "123-456-789",
                "국민은행",
                AccountType.OPERATING,
                "테스트",
                null,
                null
        );

        // when & then
        assertThatThrownBy(() -> accountService.createAccount(command))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("createAccount - 중복 계좌번호")
    void createAccount_withDuplicateAccountNumber_shouldThrow() {
        // given
        String accountNumber = "DUP-123-456";
        accountService.createAccount(new CreateAccountCommand(
                testOrg.getId(), accountNumber, "국민은행", AccountType.OPERATING, "홀더1", null, null
        ));

        // when & then
        assertThatThrownBy(() -> accountService.createAccount(new CreateAccountCommand(
                testOrg.getId(), accountNumber, "신한은행", AccountType.RESERVE, "홀더2", null, null
        ))).isInstanceOf(DuplicateException.class);
    }

    @Test
    @DisplayName("findById - ID로 계좌 조회")
    void findById_shouldReturnAccount() {
        // given
        Account created = accountService.createAccount(new CreateAccountCommand(
                testOrg.getId(), "111-222-333", "우리은행", AccountType.INVESTMENT, "투자계좌", null, "설명"
        ));

        // when
        Account found = accountService.findById(created.getId());

        // then
        assertAll(
                () -> assertThat(found.getId()).isEqualTo(created.getId()),
                () -> assertThat(found.getAccountNumber()).isEqualTo("111-222-333")
        );
    }

    @Test
    @DisplayName("findById - 존재하지 않는 ID")
    void findById_withInvalidId_shouldThrow() {
        assertThatThrownBy(() -> accountService.findById(999L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("findByOrganizationId - 조직별 계좌 목록")
    void findByOrganizationId_shouldReturnAccounts() {
        // given
        accountService.createAccount(new CreateAccountCommand(
                testOrg.getId(), "ACC-001", "국민은행", AccountType.OPERATING, "홀더", null, null
        ));
        accountService.createAccount(new CreateAccountCommand(
                testOrg.getId(), "ACC-002", "신한은행", AccountType.RESERVE, "홀더", null, null
        ));

        // when
        List<Account> accounts = accountService.findByOrganizationId(testOrg.getId());

        // then
        assertAll(
                () -> assertThat(accounts).hasSize(2),
                () -> assertThat(accounts).extracting(Account::getOrganizationId)
                        .containsOnly(testOrg.getId())
        );
    }

    @Test
    @DisplayName("delete - 계좌 삭제")
    void delete_shouldRemoveAccount() {
        // given
        Account account = accountService.createAccount(new CreateAccountCommand(
                testOrg.getId(), "DEL-123", "삭제은행", AccountType.OTHER, "홀더", null, null
        ));
        Long accountId = account.getId();

        // when
        accountService.delete(accountId);

        // then
        assertThat(accountRepository.existsById(accountId)).isFalse();
    }

    @Test
    @DisplayName("delete - 존재하지 않는 계좌 삭제")
    void delete_withInvalidId_shouldThrow() {
        assertThatThrownBy(() -> accountService.delete(999L))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
