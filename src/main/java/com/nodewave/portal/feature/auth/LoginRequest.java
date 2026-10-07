package com.nodewave.portal.feature.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Schema(description = "Email akun pengujian", example = "pm@nodewave.id")
        @NotBlank(message = "Email is required")
        String email,

        @Schema(description = "Password default seluruh akun", example = "password123")
        @NotBlank(message = "Password is required")
        String password
) {}
