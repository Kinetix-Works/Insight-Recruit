package com.insight_recruit.backend.app.dto.auth;

public record AuthUserResponse(
    String id,
    String tenantId,
    String email,
    String firstName,
    String lastName,
    String role,
    String authProvider
) {}
