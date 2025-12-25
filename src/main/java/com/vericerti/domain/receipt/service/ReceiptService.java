package com.vericerti.domain.receipt.service;

import com.vericerti.application.command.CreateReceiptCommand;
import com.vericerti.domain.common.vo.BusinessNumber;
import com.vericerti.domain.common.vo.Money;
import com.vericerti.domain.organization.repository.OrganizationRepository;
import com.vericerti.domain.receipt.entity.Receipt;
import com.vericerti.domain.receipt.repository.ReceiptRepository;
import com.vericerti.infrastructure.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReceiptService {

    private final ReceiptRepository receiptRepository;
    private final OrganizationRepository organizationRepository;

    @Transactional
    public Receipt createReceipt(CreateReceiptCommand command) {
        // 단체 존재 확인
        if (!organizationRepository.existsById(command.organizationId())) {
            throw EntityNotFoundException.organization(command.organizationId());
        }

        Receipt receipt = Receipt.builder()
                .organizationId(command.organizationId())
                .accountId(command.accountId())
                .amount(Money.of(command.amount()))
                .issueDate(command.issueDate())
                .merchantName(command.merchantName())
                .merchantBusinessNumber(command.merchantBusinessNumber() != null && !command.merchantBusinessNumber().isBlank()
                        ? BusinessNumber.of(command.merchantBusinessNumber()) : null)
                .imageUrl(command.imageUrl())
                .category(command.category())
                .description(command.description())
                .build();

        Receipt saved = receiptRepository.save(receipt);
        log.info("event=receipt_created orgId={} receiptId={} amount={}", 
                command.organizationId(), saved.getId(), command.amount());
        return saved;
    }

    @Transactional(readOnly = true)
    public Receipt findById(Long id) {
        return receiptRepository.findById(id)
                .orElseThrow(() -> EntityNotFoundException.receipt(id));
    }

    @Transactional(readOnly = true)
    public List<Receipt> findByOrganizationId(Long organizationId) {
        return receiptRepository.findByOrganizationId(organizationId);
    }

    @Transactional(readOnly = true)
    public List<Receipt> findByAccountId(Long accountId) {
        return receiptRepository.findByAccountId(accountId);
    }

    @Transactional(readOnly = true)
    public List<Receipt> findByDateRange(Long organizationId, LocalDate start, LocalDate end) {
        return receiptRepository.findByOrganizationIdAndIssueDateBetween(organizationId, start, end);
    }

    @Transactional
    public void delete(Long id) {
        if (!receiptRepository.existsById(id)) {
            throw EntityNotFoundException.receipt(id);
        }
        receiptRepository.deleteById(id);
        log.info("event=receipt_deleted receiptId={}", id);
    }
}
