package com.insight_recruit.backend.app.service;

import java.io.IOException;
import java.util.Locale;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class ResumeUploadTextExtractor {

    private final PdfTextExtractor pdfTextExtractor;
    private final DocxTextExtractor docxTextExtractor;

    public ResumeUploadTextExtractor(PdfTextExtractor pdfTextExtractor, DocxTextExtractor docxTextExtractor) {
        this.pdfTextExtractor = pdfTextExtractor;
        this.docxTextExtractor = docxTextExtractor;
    }

    public String extract(MultipartFile file) throws IOException {
        String originalName = file.getOriginalFilename();
        if (originalName == null) {
            throw new IllegalArgumentException("File name is required");
        }
        String lower = originalName.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".docx")) {
            return docxTextExtractor.extract(file);
        }
        if (lower.endsWith(".pdf")) {
            return pdfTextExtractor.extract(file);
        }
        throw new IllegalArgumentException("Only PDF and DOCX files are supported");
    }
}
