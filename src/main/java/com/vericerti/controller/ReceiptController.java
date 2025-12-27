package com.vericerti.controller;

import com.vericerti.application.command.RecordReceiptCommand;
import com.vericerti.application.dto.ReceiptResult;
import com.vericerti.application.usecase.RecordReceiptUseCase;
import com.vericerti.controller.receipt.request.ReceiptCreateRequest;
import com.vericerti.controller.receipt.response.ReceiptResponse;
import com.vericerti.domain.receipt.entity.Receipt;
import com.vericerti.domain.receipt.service.ReceiptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/organizations/{orgId}/receipts")
@RequiredArgsConstructor
public class ReceiptController {

    private final ReceiptService receiptService;
    private final RecordReceiptUseCase recordReceiptUseCase;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReceiptResponse> create(
            @PathVariable Long orgId,
            @Valid @ModelAttribute ReceiptCreateRequest request,
            @RequestParam("file") MultipartFile file) throws IOException {
        
        ReceiptResult result = recordReceiptUseCase.execute(
                new RecordReceiptCommand(
                        orgId,
                        request.accountId(),
                        request.amount(),
                        request.issueDate(),
                        request.merchantName(),
                        request.merchantBusinessNumber(),
                        request.category(),
                        request.description(),
                        file.getBytes(),
                        file.getOriginalFilename()
                )
        );
        
        return ResponseEntity
                .created(URI.create("/api/organizations/" + orgId + "/receipts/" + result.receipt().getId()))
                .body(toResponse(result.receipt(), result.ledgerEntry().getDataHashValue().orElse(null)));
    }

    @GetMapping
    public ResponseEntity<List<ReceiptResponse>> list(@PathVariable Long orgId) {
        List<ReceiptResponse> receipts = receiptService.findByOrganizationId(orgId)
                .stream()
                .map(r -> toResponse(r, null))
                .toList();
        return ResponseEntity.ok(receipts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReceiptResponse> get(@PathVariable Long orgId, @PathVariable Long id) {
        Receipt receipt = receiptService.findById(id);
        return ResponseEntity.ok(toResponse(receipt, null));
    }

    @GetMapping("/by-date")
    public ResponseEntity<List<ReceiptResponse>> listByDateRange(
            @PathVariable Long orgId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        List<ReceiptResponse> receipts = receiptService.findByDateRange(orgId, start, end)
                .stream()
                .map(r -> toResponse(r, null))
                .toList();
        return ResponseEntity.ok(receipts);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long orgId, @PathVariable Long id) {
        receiptService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private ReceiptResponse toResponse(Receipt receipt, String dataHash) {
        return new ReceiptResponse(
                receipt.getId(),
                receipt.getOrganizationId(),
                receipt.getAccountId(),
                receipt.getAmountValue(),
                receipt.getIssueDate(),
                receipt.getMerchantName(),
                receipt.getMerchantBusinessNumberValue(),
                receipt.getImageUrl(),
                receipt.getCategory(),
                receipt.getDescription(),
                receipt.getCreatedAt(),
                dataHash
        );
    }
}
