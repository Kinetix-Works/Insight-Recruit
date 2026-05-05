package com.insight_recruit.backend.app.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "insightrecruit.security")
public record SecurityProperties(
    String jwtSecret,
    long accessTokenTtlMinutes,
    long refreshTokenTtlDays,
    long otpTtlSeconds,
    int otpRateLimitMax,
    long otpRateLimitWindowSeconds,
    String refreshCookieName
) {}
