package com.vericerti.controller.organization.response;

import java.time.LocalDateTime;

public record OrganizationResponse(
        Long id,
        String name,
        String businessNumber,
        String description,
        LocalDateTime createdAt
) {}
