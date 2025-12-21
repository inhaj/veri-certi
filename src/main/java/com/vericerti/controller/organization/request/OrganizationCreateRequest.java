package com.vericerti.controller.organization.request;

import jakarta.validation.constraints.NotBlank;

public record OrganizationCreateRequest(
        @NotBlank(message = "조직명은 필수입니다")
        String name,
        
        @NotBlank(message = "사업자번호는 필수입니다")
        String businessNumber,
        
        String description
) {}
