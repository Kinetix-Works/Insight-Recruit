package com.insight_recruit.backend.app.dto.auth;

public record AuthTokenResponse(
    String accessToken,
    String tokenType,
    long expiresInSeconds,
    AuthUserResponse user
) {}
