package com.vericerti.controller.auth.request;

import com.vericerti.common.SelfValidating;
import com.vericerti.domain.member.entity.MemberRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SignupRequest extends SelfValidating<SignupRequest> {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private final String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private final String password;

    private final MemberRole role;

    public SignupRequest(String email, String password, MemberRole role) {
        this.email = email;
        this.password = password;
        this.role = role;
        validateSelf();
    }
}

