package com.insight_recruit.backend.app.worker;

import com.insight_recruit.backend.app.domain.entity.Candidate;
import com.insight_recruit.backend.app.domain.enums.ProcessingStatus;
import com.insight_recruit.backend.app.dto.AiScoreResult;
import com.insight_recruit.backend.app.dto.CandidateQueuedMessage;
import com.insight_recruit.backend.app.repository.CandidateRepository;
import com.insight_recruit.backend.app.service.LlmScoringService;
import com.insight_recruit.backend.app.service.ResumeContentReader;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CandidateProcessingWorker {

    private final CandidateRepository candidateRepository;
    private final ResumeContentReader resumeContentReader;
    private final LlmScoringService llmScoringService;

    public CandidateProcessingWorker(
        CandidateRepository candidateRepository,
        ResumeContentReader resumeContentReader,
        LlmScoringService llmScoringService
    ) {
        this.candidateRepository = candidateRepository;
        this.resumeContentReader = resumeContentReader;
        this.llmScoringService = llmScoringService;
    }

    @Async
    @Transactional
    public void processQueuedCandidate(CandidateQueuedMessage message) {
        Candidate candidate = loadCandidate(message);
        candidate.setStatus(ProcessingStatus.PROCESSING);
        candidateRepository.save(candidate);

        try {
            String resumeText = resumeContentReader.readResumeText(candidate.getResumeUrl());
            String jobDescription = candidate.getJob().getDescriptionText();

            AiScoreResult result = llmScoringService.scoreCandidate(jobDescription, resumeText);
            if (result.score() == null) {
                throw new IllegalStateException("LLM response is missing score");
            }

            candidate.setScore(BigDecimal.valueOf(Math.max(0, Math.min(100, result.score()))));
            candidate.setAiSummary(buildSummary(result));
            candidate.setStatus(ProcessingStatus.COMPLETED);
            candidateRepository.save(candidate);
        } catch (Exception exception) {
            candidate.setStatus(ProcessingStatus.FAILED);
            candidateRepository.save(candidate);
        }
    }

    private Candidate loadCandidate(CandidateQueuedMessage message) {
        Candidate candidate = candidateRepository.findById(message.candidateId())
            .orElseThrow(() -> new EntityNotFoundException("Candidate not found for id: " + message.candidateId()));

        if (!candidate.getJob().getId().equals(message.jobId())) {
            throw new IllegalStateException("Queued jobId does not match candidate's job");
        }
        return candidate;
    }

    private String buildSummary(AiScoreResult result) {
        List<String> parts = new ArrayList<>();
        if (result.summary() != null && !result.summary().isBlank()) {
            parts.add(result.summary().trim());
        }
        if (result.riskFlags() != null && !result.riskFlags().isEmpty()) {
            parts.add("Risk Flags: " + String.join(", ", result.riskFlags()));
        }
        return String.join("\n\n", parts);
    }
}
