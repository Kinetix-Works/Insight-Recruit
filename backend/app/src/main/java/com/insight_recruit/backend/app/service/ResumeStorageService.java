package com.insight_recruit.backend.app.service;

import java.io.IOException;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public interface ResumeStorageService {
    String store(UUID candidateId, MultipartFile file) throws IOException;
}
