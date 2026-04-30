package com.insight_recruit.backend.app.service;

import java.io.IOException;
import java.nio.file.Path;
import org.springframework.stereotype.Service;

@Service
public class ResumeContentReader {

    private final PdfTextExtractor pdfTextExtractor;

    public ResumeContentReader(PdfTextExtractor pdfTextExtractor) {
        this.pdfTextExtractor = pdfTextExtractor;
    }

    public String readResumeText(String resumePath) throws IOException {
        if (resumePath == null || resumePath.isBlank()) {
            throw new IllegalArgumentException("Candidate resume path is missing");
        }
        return pdfTextExtractor.extract(Path.of(resumePath));
    }
}
