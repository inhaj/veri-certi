package com.vericerti.application.command;

import com.vericerti.domain.member.entity.MemberRole;

public record SignupCommand(
        String email,
        String password,
        MemberRole role
) {}
