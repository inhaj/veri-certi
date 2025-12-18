package com.vericerti.controller.ledger.response;

public record VerifyResponse(
        boolean verified,
        String txHash,
        String dataHash,
        String message
) {}
