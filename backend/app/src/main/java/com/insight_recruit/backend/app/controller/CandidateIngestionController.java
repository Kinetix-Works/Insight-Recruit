package com.insight_recruit.backend.app.controller;

import com.insight_recruit.backend.app.dto.UploadCandidateResponse;
import com.insight_recruit.backend.app.service.CandidateIngestionService;
import java.io.IOException;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/candidates")
public class CandidateIngestionController {

    private final CandidateIngestionService candidateIngestionService;

    public CandidateIngestionController(CandidateIngestionService candidateIngestionService) {
        this.candidateIngestionService = candidateIngestionService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadCandidateResponse> upload(
        @RequestParam("file") MultipartFile file,
        @RequestParam("jobId") UUID jobId
    ) throws IOException {
        UUID candidateId = candidateIngestionService.ingest(jobId, file);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new UploadCandidateResponse(candidateId));
    }
}
