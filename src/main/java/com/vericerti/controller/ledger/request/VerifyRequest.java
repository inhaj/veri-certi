package com.vericerti.controller.ledger.request;

import jakarta.validation.constraints.NotBlank;

public record VerifyRequest(
        @NotBlank(message = "Transaction hash is required")
        String txHash,

        String dataHash
) {}





