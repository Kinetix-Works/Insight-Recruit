package com.insight_recruit.backend.app.dto;

import com.insight_recruit.backend.app.domain.enums.ProcessingStatus;
import java.math.BigDecimal;
import java.util.UUID;

public record CandidateStatusResponse(UUID candidateId, String fileName, ProcessingStatus status, BigDecimal score) {
}
