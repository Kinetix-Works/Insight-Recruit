package com.insight_recruit.backend.app.service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class ResumeContentReader {

    private final PdfTextExtractor pdfTextExtractor;
    private final DocxTextExtractor docxTextExtractor;

    public ResumeContentReader(PdfTextExtractor pdfTextExtractor, DocxTextExtractor docxTextExtractor) {
        this.pdfTextExtractor = pdfTextExtractor;
        this.docxTextExtractor = docxTextExtractor;
    }

    public String readResumeText(String resumePath) throws IOException {
        if (resumePath == null || resumePath.isBlank()) {
            throw new IllegalArgumentException("Candidate resume path is missing");
        }
        Path path = Path.of(resumePath);
        String lower = path.toString().toLowerCase(Locale.ROOT);
        if (lower.endsWith(".docx")) {
            return docxTextExtractor.extract(path);
        }
        return pdfTextExtractor.extract(path);
    }
}
