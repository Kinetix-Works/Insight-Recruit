package com.insight_recruit.backend.app.worker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insight_recruit.backend.app.dto.CandidateQueuedMessage;
import org.springframework.stereotype.Component;

@Component
public class CandidateQueueSubscriber {

    private final ObjectMapper objectMapper;
    private final CandidateProcessingWorker candidateProcessingWorker;

    public CandidateQueueSubscriber(
        ObjectMapper objectMapper,
        CandidateProcessingWorker candidateProcessingWorker
    ) {
        this.objectMapper = objectMapper;
        this.candidateProcessingWorker = candidateProcessingWorker;
    }

    public void onMessage(String payload) {
        try {
            CandidateQueuedMessage message = objectMapper.readValue(payload, CandidateQueuedMessage.class);
            candidateProcessingWorker.processQueuedCandidate(message);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to handle queue message: " + payload, exception);
        }
    }
}
