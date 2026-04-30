package com.insight_recruit.backend.app.dto;

import java.util.UUID;

public record CandidateQueuedMessage(UUID candidateId, UUID jobId) {
}
