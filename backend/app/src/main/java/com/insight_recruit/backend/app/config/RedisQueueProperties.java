package com.insight_recruit.backend.app.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "insightrecruit.queue")
public record RedisQueueProperties(String candidateUploadTopic) {
}
