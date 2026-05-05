package com.insight_recruit.backend.app.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank(message = "Email is required")
    @Email(message = "Email format is invalid")
    String email,
    @NotBlank(message = "Password is required")
    String password
) {}
