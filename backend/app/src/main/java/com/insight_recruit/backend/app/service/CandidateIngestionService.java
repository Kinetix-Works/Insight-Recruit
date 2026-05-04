package com.insight_recruit.backend.app.service;

import com.insight_recruit.backend.app.domain.entity.Candidate;
import com.insight_recruit.backend.app.domain.entity.Job;
import com.insight_recruit.backend.app.domain.enums.ProcessingStatus;
import com.insight_recruit.backend.app.dto.CandidateQueuedMessage;
import com.insight_recruit.backend.app.repository.CandidateRepository;
import com.insight_recruit.backend.app.repository.JobRepository;
import jakarta.persistence.EntityNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CandidateIngestionService {

    private final CandidateRepository candidateRepository;
    private final JobRepository jobRepository;
    private final ResumeUploadTextExtractor resumeUploadTextExtractor;
    private final ResumeStorageService resumeStorageService;
    private final CandidateQueuePublisher queuePublisher;

    public CandidateIngestionService(
        CandidateRepository candidateRepository,
        JobRepository jobRepository,
        ResumeUploadTextExtractor resumeUploadTextExtractor,
        ResumeStorageService resumeStorageService,
        CandidateQueuePublisher queuePublisher
    ) {
        this.candidateRepository = candidateRepository;
        this.jobRepository = jobRepository;
        this.resumeUploadTextExtractor = resumeUploadTextExtractor;
        this.resumeStorageService = resumeStorageService;
        this.queuePublisher = queuePublisher;
    }

    @Transactional
    public UUID ingest(UUID jobId, MultipartFile resumeFile) throws IOException {
        if (resumeFile == null || resumeFile.isEmpty()) {
            throw new IllegalArgumentException("Resume file is required");
        }
        String originalName = resumeFile.getOriginalFilename();
        if (originalName == null) {
            throw new IllegalArgumentException("Resume file name is required");
        }
        String lowerName = originalName.toLowerCase(Locale.ROOT);
        if (!lowerName.endsWith(".pdf") && !lowerName.endsWith(".docx")) {
            throw new IllegalArgumentException("Only PDF and DOCX files are supported");
        }

        Job job = jobRepository.findById(jobId)
            .orElseThrow(() -> new EntityNotFoundException("Job not found for id: " + jobId));

        String extractedText = resumeUploadTextExtractor.extract(resumeFile);
        if (extractedText == null || extractedText.isBlank()) {
            throw new IllegalArgumentException("Uploaded PDF does not contain extractable text");
        }
        UUID candidateId = UUID.randomUUID();
        String resumePath = resumeStorageService.store(candidateId, resumeFile);

        Candidate candidate = Candidate.builder()
            .id(candidateId)
            .tenant(job.getTenant())
            .job(job)
            .name(resolveCandidateName(originalName))
            .resumeUrl(resumePath)
            .status(ProcessingStatus.PENDING)
            .createdAt(LocalDateTime.now())
            .build();

        candidateRepository.save(candidate);

        // Queue payload stays compact per contract (candidateId + jobId).
        queuePublisher.publish(new CandidateQueuedMessage(candidate.getId(), job.getId()));

        return candidate.getId();
    }

    private String resolveCandidateName(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return "Unknown Candidate";
        }
        int dotIndex = originalFilename.lastIndexOf('.');
        return dotIndex > 0 ? originalFilename.substring(0, dotIndex) : originalFilename;
    }
}
