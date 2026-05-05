package com.insight_recruit.backend.app.auth;

public record NormalizedIdentifier(AuthIdentifierType type, String normalizedValue) {}
