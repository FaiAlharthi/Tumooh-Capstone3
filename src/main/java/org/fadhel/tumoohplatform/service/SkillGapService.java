package org.fadhel.tumoohplatform.service;


import lombok.RequiredArgsConstructor;
import org.fadhel.tumoohplatform.dto.in.SkillGapRequest;
import org.fadhel.tumoohplatform.dto.out.SkillGapResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class SkillGapService {

    private final GeminiService geminiService;
    private final ObjectMapper objectMapper;

    public SkillGapResponse analyzeSkillGap(SkillGapRequest request) {
        String prompt = String.format("""
            Act as an expert career coach and talent development consultant.
            Perform a skill gap analysis for a candidate targeting the role of '%s'.

            Candidate Summary (current skills, experience, education): %s

            Job Details / Context: %s

            Skills the candidate specifically wants to focus on: %s

            Strictly return ONLY a valid JSON object matching this exact schema (no markdown formatting, no text outside JSON):
            {
              "readinessScore": 65,
              "currentStrengths": ["Strength 1 aligned to the target role", "Strength 2"],
              "gaps": [
                {"skill": "Missing skill 1", "priority": "HIGH", "reason": "Why this gap matters for the target role"},
                {"skill": "Missing skill 2", "priority": "MEDIUM", "reason": "Why this gap matters for the target role"}
              ],
              "learningPlan": [
                {"skill": "Missing skill 1", "suggestion": "Concrete learning suggestion: course type, certification, or practice project"}
              ],
              "overallAssessment": "Summary of overall readiness, realistic timeline and strategy to close the gaps."
            }
            """,
                request.getTargetRole(),
                (request.getCandidateSummary() != null ? request.getCandidateSummary() : "A motivated professional, skills not specified"),
                (request.getJobDescription() != null ? request.getJobDescription() : "Standard industry requirements for this role"),
                (request.getHighlightedSkills() != null ? request.getHighlightedSkills() : "No specific focus, cover all critical gaps for the role")
        );

        String rawAiOutput = geminiService.generateText(prompt);

        try {
            String cleanJson = rawAiOutput.replaceAll("```json|```", "").trim();

            SkillGapResponse response = objectMapper.readValue(cleanJson, SkillGapResponse.class);
            response.setTargetRole(request.getTargetRole());
            return response;

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "AI generation failed. Please try again.");
        }
    }

}
