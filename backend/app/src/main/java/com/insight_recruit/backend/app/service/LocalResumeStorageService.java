package com.insight_recruit.backend.app.service;

import com.insight_recruit.backend.app.config.StorageProperties;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class LocalResumeStorageService implements ResumeStorageService {

    private final StorageProperties storageProperties;

    public LocalResumeStorageService(StorageProperties storageProperties) {
        this.storageProperties = storageProperties;
    }

    @Override
    public String store(UUID candidateId, MultipartFile file) throws IOException {
        String uploadDir = storageProperties.uploadDir();
        Path directory = Path.of(uploadDir == null || uploadDir.isBlank() ? "uploads/resumes" : uploadDir);
        Files.createDirectories(directory);

        String originalName = file.getOriginalFilename();
        String extension = ".pdf";
        if (originalName != null) {
            String lower = originalName.toLowerCase(Locale.ROOT);
            if (lower.endsWith(".pdf")) {
                extension = ".pdf";
            }
        }

        Path target = directory.resolve(candidateId + extension);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        return target.toAbsolutePath().toString();
    }
}
