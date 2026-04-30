package com.insight_recruit.backend.app.service;

import com.insight_recruit.backend.app.dto.AiScoreResult;

public interface LlmScoringService {
    AiScoreResult scoreCandidate(String jobDescription, String resumeText);
}
