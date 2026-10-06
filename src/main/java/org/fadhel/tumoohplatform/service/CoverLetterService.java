package org.fadhel.tumoohplatform.service;


import lombok.RequiredArgsConstructor;
import org.fadhel.tumoohplatform.dto.in.CoverLetterRequest;
import org.fadhel.tumoohplatform.dto.out.CoverLetterResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class CoverLetterService {

    private final GeminiService geminiService;
    private final ObjectMapper objectMapper;

    public CoverLetterResponse generateCoverLetter(CoverLetterRequest request) {
        String prompt = String.format("""
            Act as an expert career writer and professional cover letter coach.
            Write a tailored cover letter for %s applying for the role of '%s' %s.

            Job Details / Context: %s

            Candidate Summary (skills, experience, achievements): %s

            Desired tone: %s

            Strictly return ONLY a valid JSON object matching this exact schema (no markdown formatting, no text outside JSON):
            {
              "subjectLine": "Professional email subject line for the application",
              "coverLetter": "The complete cover letter text, well structured with greeting, body paragraphs and closing.",
              "keyPointsUsed": ["Key candidate strength matched to the role 1", "Key candidate strength matched to the role 2"],
              "tone": "The tone actually used, e.g. formal and confident"
            }
            """,
                request.getCandidateName(),
                request.getJobTitle(),
                (request.getTargetCompany() != null ? "at " + request.getTargetCompany() : ""),
                (request.getJobDescription() != null ? request.getJobDescription() : "Standard industry requirements"),
                (request.getCandidateSummary() != null ? request.getCandidateSummary() : "A motivated professional relevant to this role"),
                (request.getTone() != null ? request.getTone() : "professional and confident")
        );

        String rawAiOutput = geminiService.generateText(prompt);

        try {
            String cleanJson = rawAiOutput.replaceAll("```json|```", "").trim();

            CoverLetterResponse response = objectMapper.readValue(cleanJson, CoverLetterResponse.class);
            response.setJobTitle(request.getJobTitle());
            response.setTargetCompany(request.getTargetCompany());
            return response;

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "AI generation failed. Please try again.");
        }
    }

}
