package com.vericerti.controller.auth.request;

import com.vericerti.common.SelfValidating;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginRequest extends SelfValidating<LoginRequest> {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private final String email;

    @NotBlank(message = "Password is required")
    private final String password;

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
        validateSelf();
    }
}

