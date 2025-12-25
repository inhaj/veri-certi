package com.vericerti.domain.receipt.service;

import com.vericerti.application.command.CreateReceiptCommand;
import com.vericerti.config.BaseIntegrationTest;
import com.vericerti.domain.account.entity.Account;
import com.vericerti.domain.account.entity.AccountType;
import com.vericerti.domain.account.repository.AccountRepository;
import com.vericerti.domain.common.vo.BusinessNumber;
import com.vericerti.domain.organization.entity.Organization;
import com.vericerti.domain.organization.repository.OrganizationRepository;
import com.vericerti.domain.receipt.entity.Receipt;
import com.vericerti.domain.receipt.entity.ReceiptCategory;
import com.vericerti.domain.receipt.repository.ReceiptRepository;
import com.vericerti.infrastructure.exception.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class ReceiptServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ReceiptService receiptService;

    @Autowired
    private ReceiptRepository receiptRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private AccountRepository accountRepository;

    private Organization testOrg;
    private Account testAccount;

    @BeforeEach
    void setUp() {
        testOrg = organizationRepository.save(Organization.builder()
                .name("테스트 단체")
                .businessNumber(BusinessNumber.of("BN-" + UUID.randomUUID()))
                .description("테스트용")
                .build());

        testAccount = accountRepository.save(Account.builder()
                .organizationId(testOrg.getId())
                .accountNumber("TEST-ACC-" + UUID.randomUUID())
                .bankName("테스트은행")
                .accountType(AccountType.OPERATING)
                .accountHolder("테스트")
                .build());
    }

    @Test
    @DisplayName("createReceipt - 영수증 생성 성공")
    void createReceipt_shouldSaveReceipt() {
        // given
        CreateReceiptCommand command = new CreateReceiptCommand(
                testOrg.getId(),
                testAccount.getId(),
                new BigDecimal("50000.00"),
                LocalDate.of(2024, 1, 15),
                "스타벅스",
                "123-45-67890",
                "http://storage/receipt.jpg",
                ReceiptCategory.OFFICE,
                "사무용품 구매"
        );

        // when
        Receipt receipt = receiptService.createReceipt(command);

        // then
        assertAll(
                () -> assertThat(receipt.getId()).isNotNull(),
                () -> assertThat(receipt.getOrganizationId()).isEqualTo(testOrg.getId()),
                () -> assertThat(receipt.getAccountId()).isEqualTo(testAccount.getId()),
                () -> assertThat(receipt.getAmountValue()).isEqualByComparingTo(new BigDecimal("50000.00")),
                () -> assertThat(receipt.getIssueDate()).isEqualTo(LocalDate.of(2024, 1, 15)),
                () -> assertThat(receipt.getMerchantName()).isEqualTo("스타벅스"),
                () -> assertThat(receipt.getCategory()).isEqualTo(ReceiptCategory.OFFICE),
                () -> assertThat(receipt.getCreatedAt()).isNotNull()
        );
    }

    @Test
    @DisplayName("createReceipt - 존재하지 않는 조직")
    void createReceipt_withInvalidOrg_shouldThrow() {
        // given
        CreateReceiptCommand command = new CreateReceiptCommand(
                999L, null, new BigDecimal("10000"), LocalDate.now(),
                "테스트", null, null, ReceiptCategory.OTHER, null
        );

        // when & then
        assertThatThrownBy(() -> receiptService.createReceipt(command))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("findById - ID로 영수증 조회")
    void findById_shouldReturnReceipt() {
        // given
        Receipt created = receiptService.createReceipt(new CreateReceiptCommand(
                testOrg.getId(), null, new BigDecimal("30000"), LocalDate.now(),
                "CU편의점", null, null, ReceiptCategory.UTILITIES, "간식비"
        ));

        // when
        Receipt found = receiptService.findById(created.getId());

        // then
        assertAll(
                () -> assertThat(found.getId()).isEqualTo(created.getId()),
                () -> assertThat(found.getMerchantName()).isEqualTo("CU편의점")
        );
    }

    @Test
    @DisplayName("findById - 존재하지 않는 ID")
    void findById_withInvalidId_shouldThrow() {
        assertThatThrownBy(() -> receiptService.findById(999L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("findByOrganizationId - 조직별 영수증 목록")
    void findByOrganizationId_shouldReturnReceipts() {
        // given
        receiptService.createReceipt(new CreateReceiptCommand(
                testOrg.getId(), null, new BigDecimal("10000"), LocalDate.now(),
                "상점1", null, null, ReceiptCategory.OFFICE, null
        ));
        receiptService.createReceipt(new CreateReceiptCommand(
                testOrg.getId(), null, new BigDecimal("20000"), LocalDate.now(),
                "상점2", null, null, ReceiptCategory.UTILITIES, null
        ));

        // when
        List<Receipt> receipts = receiptService.findByOrganizationId(testOrg.getId());

        // then
        assertAll(
                () -> assertThat(receipts).hasSize(2),
                () -> assertThat(receipts).extracting(Receipt::getOrganizationId)
                        .containsOnly(testOrg.getId())
        );
    }

    @Test
    @DisplayName("findByAccountId - 계좌별 영수증 목록")
    void findByAccountId_shouldReturnReceipts() {
        // given
        receiptService.createReceipt(new CreateReceiptCommand(
                testOrg.getId(), testAccount.getId(), new BigDecimal("15000"), LocalDate.now(),
                "상점A", null, null, ReceiptCategory.OFFICE, null
        ));
        receiptService.createReceipt(new CreateReceiptCommand(
                testOrg.getId(), testAccount.getId(), new BigDecimal("25000"), LocalDate.now(),
                "상점B", null, null, ReceiptCategory.UTILITIES, null
        ));

        // when
        List<Receipt> receipts = receiptService.findByAccountId(testAccount.getId());

        // then
        assertAll(
                () -> assertThat(receipts).hasSize(2),
                () -> assertThat(receipts).extracting(Receipt::getAccountId)
                        .containsOnly(testAccount.getId())
        );
    }

    @Test
    @DisplayName("findByDateRange - 날짜 범위 조회")
    void findByDateRange_shouldReturnReceiptsInRange() {
        // given
        LocalDate jan10 = LocalDate.of(2024, 1, 10);
        LocalDate jan20 = LocalDate.of(2024, 1, 20);
        LocalDate jan30 = LocalDate.of(2024, 1, 30);

        receiptService.createReceipt(new CreateReceiptCommand(
                testOrg.getId(), null, new BigDecimal("10000"), jan10,
                "1월10일", null, null, ReceiptCategory.OTHER, null
        ));
        receiptService.createReceipt(new CreateReceiptCommand(
                testOrg.getId(), null, new BigDecimal("20000"), jan20,
                "1월20일", null, null, ReceiptCategory.OTHER, null
        ));
        receiptService.createReceipt(new CreateReceiptCommand(
                testOrg.getId(), null, new BigDecimal("30000"), jan30,
                "1월30일", null, null, ReceiptCategory.OTHER, null
        ));

        // when
        List<Receipt> receipts = receiptService.findByDateRange(
                testOrg.getId(),
                LocalDate.of(2024, 1, 15),
                LocalDate.of(2024, 1, 25)
        );

        // then
        assertAll(
                () -> assertThat(receipts).hasSize(1),
                () -> assertThat(receipts.get(0).getMerchantName()).isEqualTo("1월20일")
        );
    }

    @Test
    @DisplayName("delete - 영수증 삭제")
    void delete_shouldRemoveReceipt() {
        // given
        Receipt receipt = receiptService.createReceipt(new CreateReceiptCommand(
                testOrg.getId(), null, new BigDecimal("5000"), LocalDate.now(),
                "삭제대상", null, null, ReceiptCategory.OTHER, null
        ));
        Long receiptId = receipt.getId();

        // when
        receiptService.delete(receiptId);

        // then
        assertThat(receiptRepository.existsById(receiptId)).isFalse();
    }

    @Test
    @DisplayName("delete - 존재하지 않는 영수증 삭제")
    void delete_withInvalidId_shouldThrow() {
        assertThatThrownBy(() -> receiptService.delete(999L))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
