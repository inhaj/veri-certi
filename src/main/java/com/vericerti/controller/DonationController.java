package com.vericerti.controller;

import com.vericerti.application.command.RecordDonationCommand;
import com.vericerti.application.dto.DonationResult;
import com.vericerti.application.usecase.RecordDonationUseCase;
import com.vericerti.controller.donation.request.DonationCreateRequest;
import com.vericerti.controller.donation.response.DonationResponse;
import com.vericerti.domain.donation.entity.Donation;
import com.vericerti.domain.donation.service.DonationService;
import com.vericerti.domain.ledger.entity.LedgerEntry;
import com.vericerti.domain.member.entity.Member;
import com.vericerti.domain.member.service.MemberService;
import jakarta.validation.Valid;
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
    private final MemberService memberService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DonationResponse> createDonation(
            @PathVariable Long orgId,
            @Valid @RequestPart("donation") DonationCreateRequest request,
            @RequestPart("receipt") MultipartFile receiptFile,
            @AuthenticationPrincipal UserDetails userDetails) throws IOException {

        Member member = memberService.findByEmail(userDetails.getUsername());

        DonationResult result = recordDonationUseCase.execute(
                new RecordDonationCommand(
                        orgId,
                        member.getId(),
                        request.amount(),
                        request.purpose(),
                        receiptFile.getBytes(),
                        receiptFile.getOriginalFilename()
                )
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
        // Directly query Donation as independent Aggregate
        Donation donation = donationService.findById(id);
        return ResponseEntity.ok(toResponse(donation, null));
    }

    private DonationResponse toResponse(Donation donation, LedgerEntry ledgerEntry) {
        DonationResponse.LedgerInfo ledgerInfo = null;
        if (ledgerEntry != null) {
            ledgerInfo = new DonationResponse.LedgerInfo(
                    ledgerEntry.getId(),
                    ledgerEntry.getDataHashValue().orElse(null),
                    ledgerEntry.getBlockchainTxHash(),
                    ledgerEntry.getStatus()
            );
        }

        return new DonationResponse(
                donation.getId(),
                donation.getOrganizationId(),
                donation.getMemberId(),
                donation.getAmountValue().orElse(null),
                donation.getPurpose(),
                donation.getDonatedAt(),
                ledgerInfo
        );
    }
}


