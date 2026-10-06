package org.fadhel.tumoohplatform.service;


import lombok.RequiredArgsConstructor;
import org.fadhel.tumoohplatform.dto.in.LinkedInProfileRequest;
import org.fadhel.tumoohplatform.dto.out.LinkedInProfileResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class LinkedInProfileService {

    private final GeminiService geminiService;
    private final ObjectMapper objectMapper;

    public LinkedInProfileResponse generateLinkedInProfile(LinkedInProfileRequest request) {
        String prompt = String.format("""
            Act as an expert LinkedIn branding and personal branding consultant.
            Create LinkedIn profile content for %s.

            Current title: %s

            Target role: %s

            Skills: %s

            Years of experience: %s

            Desired tone: %s

            Strictly return ONLY a valid JSON object matching this exact schema (no markdown formatting, no text outside JSON):
            {
              "headlines": [
                "Headline variant 1 - recruiter focused, keyword rich",
                "Headline variant 2 - achievement led",
                "Headline variant 3 - keyword optimized for LinkedIn search"
              ],
              "aboutSection": "A compelling multi-paragraph LinkedIn About section: hook opening, what they do, key strengths, achievements, what they are looking for, and a call to action.",
              "keywords": ["keyword 1", "keyword 2", "keyword 3"],
              "tipOnUsage": "Short advice on which headline variant to pick and how to rotate them."
            }
            """,
                request.getCandidateName(),
                (request.getCurrentTitle() != null ? request.getCurrentTitle() : "Not specified"),
                (request.getTargetRole() != null ? request.getTargetRole() : "Open to relevant opportunities"),
                (request.getSkills() != null ? request.getSkills() : "Not specified"),
                (request.getYearsOfExperience() != null
                        ? request.getYearsOfExperience() + " years"
                        : "Not specified"),
                (request.getTone() != null ? request.getTone() : "professional")
        );

        String rawAiOutput = geminiService.generateText(prompt);

        try {
            String cleanJson = rawAiOutput.replaceAll("```json|```", "").trim();

            LinkedInProfileResponse response = objectMapper.readValue(cleanJson, LinkedInProfileResponse.class);
            response.setCandidateName(request.getCandidateName());
            return response;

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "AI generation failed. Please try again.");
        }
    }

}
