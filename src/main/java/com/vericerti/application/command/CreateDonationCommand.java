package com.vericerti.application.command;

import java.math.BigDecimal;

public record CreateDonationCommand(
        Long organizationId,
        Long memberId,
        BigDecimal amount,
        String purpose
) {}
