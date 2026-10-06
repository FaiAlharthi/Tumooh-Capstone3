package org.fadhel.tumoohplatform.service;


import lombok.RequiredArgsConstructor;
import org.fadhel.tumoohplatform.dto.in.CareerPivotRequest;
import org.fadhel.tumoohplatform.dto.out.CareerPivotResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class CareerPivotService {

    private final GeminiService geminiService;
    private final ObjectMapper objectMapper;

    public CareerPivotResponse analyzeCareerPivot(CareerPivotRequest request) {
        String prompt = String.format("""
            Act as an expert career transition coach.
            Assess the feasibility of a candidate pivoting to the role of '%s'.

            Candidate Summary (current role, skills, experience, education): %s

            Years of experience: %s

            Job Details / Context for the target role: %s

            Strictly return ONLY a valid JSON object matching this exact schema (no markdown formatting, no text outside JSON):
            {
              "feasibilityScore": 70,
              "transferableSkills": ["Current skill 1 that transfers to the target role", "Current skill 2"],
              "gaps": ["Skill or credential missing for the target role", "Another gap"],
              "pivotPaths": [
                {"path": "Most realistic pathway name in ranked order", "difficulty": "MEDIUM", "estimatedTimeline": "9-12 months", "steps": ["Actionable step 1", "Actionable step 2"]},
                {"path": "Second pathway", "difficulty": "HIGH", "estimatedTimeline": "18-24 months", "steps": ["Step 1", "Step 2"]}
              ],
              "risks": ["Key risk or barrier to the pivot", "Another risk"],
              "overallVerdict": "Honest summary of feasibility, realistic expectations, and recommended first step."
            }
            """,
                request.getTargetRole(),
                (request.getCandidateSummary() != null ? request.getCandidateSummary() : "A motivated professional, background not specified"),
                (request.getYearsOfExperience() != null
                        ? request.getYearsOfExperience() + " years"
                        : "Not specified"),
                (request.getJobDescription() != null ? request.getJobDescription() : "Standard requirements for the target role")
        );

        String rawAiOutput = geminiService.generateText(prompt);

        try {
            String cleanJson = rawAiOutput.replaceAll("```json|```", "").trim();

            CareerPivotResponse response = objectMapper.readValue(cleanJson, CareerPivotResponse.class);
            response.setTargetRole(request.getTargetRole());
            return response;

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "AI generation failed. Please try again.");
        }
    }

}