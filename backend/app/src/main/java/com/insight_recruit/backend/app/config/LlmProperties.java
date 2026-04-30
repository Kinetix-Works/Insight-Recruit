package com.insight_recruit.backend.app.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "insightrecruit.ai")
public record LlmProperties(
    String llmApiKey,
    String baseUrl,
    String model,
    Double temperature
) {
}
