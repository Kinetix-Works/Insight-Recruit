package com.insight_recruit.backend.app.service;

import com.insight_recruit.backend.app.domain.entity.Candidate;
import com.insight_recruit.backend.app.dto.CandidateStatusResponse;
import com.insight_recruit.backend.app.repository.CandidateRepository;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CandidateQueryService {

    private final CandidateRepository candidateRepository;

    public CandidateQueryService(CandidateRepository candidateRepository) {
        this.candidateRepository = candidateRepository;
    }

    @Transactional(readOnly = true)
    public List<CandidateStatusResponse> listByJobId(UUID jobId) {
        return candidateRepository.findByJob_Id(jobId).stream()
            .map(this::toResponse)
            .toList();
    }

    private CandidateStatusResponse toResponse(Candidate candidate) {
        String resumePath = candidate.getResumeUrl() == null ? "" : candidate.getResumeUrl().toLowerCase(Locale.ROOT);
        String extension = resumePath.endsWith(".docx") ? ".docx" : ".pdf";
        String baseName = candidate.getName() != null && !candidate.getName().isBlank()
            ? candidate.getName()
            : "resume";
        return new CandidateStatusResponse(
            candidate.getId(),
            baseName + extension,
            candidate.getStatus(),
            candidate.getScore()
        );
    }
}
