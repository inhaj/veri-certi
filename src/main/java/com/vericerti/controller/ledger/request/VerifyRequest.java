package com.vericerti.controller.ledger.request;

import com.vericerti.common.SelfValidating;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VerifyRequest extends SelfValidating<VerifyRequest> {

    @NotBlank(message = "Transaction hash is required")
    private final String txHash;

    private final String dataHash;

    public VerifyRequest(String txHash, String dataHash) {
        this.txHash = txHash;
        this.dataHash = dataHash;
        validateSelf();
    }
}

