package com.vericerti.application.command;

public record RecordDonationCommand(
        Long organizationId,
        Long memberId,
        java.math.BigDecimal amount,
        String purpose,
        byte[] receiptFile,
        String filename
) {}
