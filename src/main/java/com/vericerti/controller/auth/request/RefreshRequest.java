package com.vericerti.controller.auth.request;

import com.vericerti.common.SelfValidating;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RefreshRequest extends SelfValidating<RefreshRequest> {

    @NotBlank(message = "Refresh token is required")
    private final String refreshToken;

    public RefreshRequest(String refreshToken) {
        this.refreshToken = refreshToken;
        validateSelf();
    }
}

