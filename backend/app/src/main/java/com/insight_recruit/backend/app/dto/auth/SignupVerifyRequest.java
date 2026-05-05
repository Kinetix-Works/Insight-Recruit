package com.insight_recruit.backend.app.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SignupVerifyRequest(
    @NotBlank(message = "Email is required")
    @Email(message = "Email format is invalid")
    String email,
    @NotBlank(message = "OTP is required")
    @Pattern(regexp = "^\\d{6}$", message = "OTP must be a 6-digit code")
    String otp
) {}
