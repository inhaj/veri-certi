package com.vericerti.controller;

import com.vericerti.application.dto.DonationResult;
import com.vericerti.application.usecase.RecordDonationUseCase;
import com.vericerti.controller.donation.request.DonationCreateRequest;
import com.vericerti.controller.donation.response.DonationResponse;
import com.vericerti.domain.donation.entity.Donation;
import com.vericerti.domain.donation.service.DonationService;
import com.vericerti.domain.ledger.entity.LedgerEntry;
import com.vericerti.domain.member.entity.Member;
import com.vericerti.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/organizations/{orgId}/donations")
@RequiredArgsConstructor
public class DonationController {

    private final DonationService donationService;
    private final RecordDonationUseCase recordDonationUseCase;
    private final MemberRepository memberRepository;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DonationResponse> createDonation(
            @PathVariable Long orgId,
            @RequestPart("donation") DonationCreateRequest request,
            @RequestPart("receipt") MultipartFile receiptFile,
            @AuthenticationPrincipal UserDetails userDetails) throws IOException {

        Member member = memberRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        DonationResult result = recordDonationUseCase.execute(
                orgId,
                member.getId(),
                request.getAmount(),
                request.getPurpose(),
                receiptFile.getBytes(),
                receiptFile.getOriginalFilename()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toResponse(result.donation(), result.ledgerEntry()));
    }

    @GetMapping
    public ResponseEntity<List<DonationResponse>> getDonations(@PathVariable Long orgId) {
        List<Donation> donations = donationService.findByOrganization(orgId);
        List<DonationResponse> responses = donations.stream()
                .map(d -> toResponse(d, null))
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DonationResponse> getDonation(
            @PathVariable Long orgId,
            @PathVariable Long id) {
        Donation donation = donationService.findById(id);
        
        // 조직 검증: 해당 기부가 요청한 조직의 것인지 확인
        if (!donation.getOrganizationId().equals(orgId)) {
            throw new IllegalArgumentException("Donation does not belong to this organization");
        }
        
        return ResponseEntity.ok(toResponse(donation, null));
    }

    private DonationResponse toResponse(Donation donation, LedgerEntry ledgerEntry) {
        DonationResponse.LedgerInfo ledgerInfo = null;
        if (ledgerEntry != null) {
            ledgerInfo = new DonationResponse.LedgerInfo(
                    ledgerEntry.getId(),
                    ledgerEntry.getDataHash(),
                    ledgerEntry.getBlockchainTxHash(),
                    ledgerEntry.getStatus()
            );
        }

        return new DonationResponse(
                donation.getId(),
                donation.getOrganizationId(),
                donation.getMemberId(),
                donation.getAmount(),
                donation.getPurpose(),
                donation.getDonatedAt(),
                ledgerInfo
        );
    }
}

