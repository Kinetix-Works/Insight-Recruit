package com.insight_recruit.backend.app.service;

import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;

public interface PdfTextExtractor {
    String extract(MultipartFile file) throws IOException;
}
