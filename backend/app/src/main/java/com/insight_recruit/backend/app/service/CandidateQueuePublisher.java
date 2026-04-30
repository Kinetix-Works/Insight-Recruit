package com.insight_recruit.backend.app.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insight_recruit.backend.app.config.RedisQueueProperties;
import com.insight_recruit.backend.app.dto.CandidateQueuedMessage;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class CandidateQueuePublisher {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final RedisQueueProperties queueProperties;

    public CandidateQueuePublisher(
        StringRedisTemplate redisTemplate,
        ObjectMapper objectMapper,
        RedisQueueProperties queueProperties
    ) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.queueProperties = queueProperties;
    }

    public void publish(CandidateQueuedMessage message) {
        String topic = queueProperties.candidateUploadTopic();
        String resolvedTopic = (topic == null || topic.isBlank()) ? "insightrecruit:candidates:uploaded" : topic;

        try {
            String payload = objectMapper.writeValueAsString(message);
            redisTemplate.convertAndSend(resolvedTopic, payload);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize candidate queue message", exception);
        }
    }
}
