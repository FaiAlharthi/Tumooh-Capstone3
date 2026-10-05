package org.fadhel.tumoohplatform.service;


import lombok.RequiredArgsConstructor;
import org.fadhel.tumoohplatform.dto.in.InterviewPreparationRequest;
import org.fadhel.tumoohplatform.dto.out.InterviewPreparationResponse;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class InterviewPreparationService {

    private final GeminiService geminiService;
    private final ObjectMapper objectMapper;

    public InterviewPreparationResponse generatePreparationGuide(InterviewPreparationRequest request) {
        String prompt = String.format("""
            Act as an expert technical career coach. Generate interview preparation tips for an applicant applying for the role of '%s' %s.
            
            Job Details / Context: %s
            
            Strictly return ONLY a valid JSON object matching this exact schema (no markdown formatting, no text outside JSON):
            {
              "keyPreparationTips": ["tip 1", "tip 2", "tip 3"],
              "commonInterviewQuestions": ["question 1", "question 2", "question 3"],
              "recommendedSkillsToHighlight": ["skill 1", "skill 2", "skill 3"],
              "overallStrategy": "Detailed strategy summary here."
            }
            """,
                request.getJobTitle(),
                (request.getTargetCompany() != null ? "at " + request.getTargetCompany() : ""),
                (request.getJobDescription() != null ? request.getJobDescription() : "Standard industry requirements")
        );

        String rawAiOutput = geminiService.generateText(prompt);

        try {
            String cleanJson = rawAiOutput.replaceAll("```json|```", "").trim();

            InterviewPreparationResponse response = objectMapper.readValue(cleanJson, InterviewPreparationResponse.class);
            response.setJobTitle(request.getJobTitle());
            response.setTargetCompany(request.getTargetCompany());
            return response;

        } catch (Exception e) {
            return InterviewPreparationResponse.builder()
                    .jobTitle(request.getJobTitle())
                    .targetCompany(request.getTargetCompany())
                    .overallStrategy("Review core technical concepts and practice STAR method responses for behavioral questions.")
                    .build();
        }
    }

}
