package com.insight_recruit.backend.app.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record RequestOtpRequest(
    @NotBlank(message = "Identifier is required")
    String identifier
) {}
