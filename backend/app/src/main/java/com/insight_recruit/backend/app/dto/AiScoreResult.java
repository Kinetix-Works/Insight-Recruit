package com.insight_recruit.backend.app.dto;

import java.util.List;

public record AiScoreResult(
    Integer score,
    String summary,
    List<String> riskFlags
) {
}
