package com.insight_recruit.backend.app.dto.auth;

import com.insight_recruit.backend.app.domain.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SignupInitiateRequest(
    @NotBlank(message = "Email is required")
    @Email(message = "Email format is invalid")
    String email,
    @NotBlank(message = "First name is required")
    String firstName,
    @NotBlank(message = "Last name is required")
    String lastName,
    @NotNull(message = "Role is required")
    UserRole role
) {}
