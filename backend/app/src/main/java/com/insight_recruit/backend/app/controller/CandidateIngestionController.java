package com.insight_recruit.backend.app.controller;

import com.insight_recruit.backend.app.dto.CandidateStatusResponse;
import com.insight_recruit.backend.app.dto.UploadCandidateResponse;
import com.insight_recruit.backend.app.service.CandidateIngestionService;
import com.insight_recruit.backend.app.service.CandidateQueryService;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/candidates")
public class CandidateIngestionController {

    private final CandidateIngestionService candidateIngestionService;
    private final CandidateQueryService candidateQueryService;

    public CandidateIngestionController(
        CandidateIngestionService candidateIngestionService,
        CandidateQueryService candidateQueryService
    ) {
        this.candidateIngestionService = candidateIngestionService;
        this.candidateQueryService = candidateQueryService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'RECRUITER', 'VIEWER')")
    public ResponseEntity<List<CandidateStatusResponse>> listByJob(@RequestParam("jobId") UUID jobId) {
        return ResponseEntity.ok(candidateQueryService.listByJobId(jobId));
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'RECRUITER')")
    public ResponseEntity<UploadCandidateResponse> upload(
        @RequestParam("file") MultipartFile file,
        @RequestParam("jobId") UUID jobId
    ) throws IOException {
        UUID candidateId = candidateIngestionService.ingest(jobId, file);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new UploadCandidateResponse(candidateId));
    }
}
