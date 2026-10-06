package org.fadhel.tumoohplatform.service;


import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.fadhel.tumoohplatform.Api.ApiException;
import org.fadhel.tumoohplatform.dto.out.CvRevisionResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class CvRevisionService {

    private final GeminiService geminiService;
    private final ObjectMapper objectMapper;

    public CvRevisionResponse reviseCvPdf(MultipartFile file) {
        String extractedCvText = extractTextFromPdf(file);

        String prompt = String.format("""
            Act as an expert ATS auditor and professional resume coach.
            Analyze and revise the following candidate CV text.

            CV Content:
            %s

            Strictly return ONLY a valid JSON object matching this exact schema (no markdown formatting, no text outside JSON):
            {
              "atsScore": 85,
              "keyStrengths": ["Clear technical stack", "Good structural flow"],
              "areasForImprovement": ["Lack of quantifiable metrics", "Action verbs could be stronger"],
              "revisedProfessionalSummary": "Improved ATS-optimized professional summary...",
              "revisedBulletPoints": ["Rewritten bullet point 1 using strong action verbs", "Rewritten bullet point 2"],
              "formattingAndToneFeedback": "Advice on overall impact, action verbs, and section balance."
            }
            """,
                extractedCvText
        );

        String rawAiOutput = geminiService.generateText(prompt);

        try {
            String cleanJson = rawAiOutput.replaceAll("```json|```", "").trim();
            CvRevisionResponse response = objectMapper.readValue(cleanJson, CvRevisionResponse.class);
            response.setFileName(file.getOriginalFilename());
            return response;
        } catch (Exception e) {
            return CvRevisionResponse.builder()
                    .fileName(file.getOriginalFilename())
                    .atsScore(0)
                    .formattingAndToneFeedback("Error parsing AI feedback. Ensure the PDF file contains readable text.")
                    .build();
        }
    }

    private String extractTextFromPdf(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApiException("PDF file is required and cannot be empty.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals("application/pdf")) {
            throw new ApiException("Only PDF files are supported.");
        }

        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        } catch (IOException e) {
            throw new RuntimeException("Error reading content from PDF file: " + file.getOriginalFilename(), e);
        }
    }
}
