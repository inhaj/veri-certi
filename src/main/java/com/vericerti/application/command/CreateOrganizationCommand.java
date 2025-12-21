package com.vericerti.application.command;

public record CreateOrganizationCommand(
        String name,
        String businessNumber,
        String description
) {}
