package com.vericerti.controller.donation.request;

import com.vericerti.common.SelfValidating;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class DonationCreateRequest extends SelfValidating<DonationCreateRequest> {

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private final BigDecimal amount;

    private final String purpose;

    public DonationCreateRequest(BigDecimal amount, String purpose) {
        this.amount = amount;
        this.purpose = purpose;
        validateSelf();
    }
}

