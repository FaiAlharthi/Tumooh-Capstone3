package org.fadhel.tumoohplatform.service;

import lombok.RequiredArgsConstructor;
import org.fadhel.tumoohplatform.Api.ApiException;
import org.fadhel.tumoohplatform.dto.out.NextStepResponse;
import org.fadhel.tumoohplatform.model.Interview;
import org.fadhel.tumoohplatform.model.JobApplication;
import org.fadhel.tumoohplatform.model.User;
import org.fadhel.tumoohplatform.repository.JobApplicationRepository;
import org.fadhel.tumoohplatform.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class suggestNextStepService {

    private final UserRepository userRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final GeminiService geminiService;
    private final ObjectMapper objectMapper;

    public NextStepResponse suggestNextStep(Long userId, Long applicationId) {
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ApiException("User not found");
        }

        JobApplication application = jobApplicationRepository.findJobApplicationById(applicationId);
        if (application == null) {
            throw new ApiException("Job application not found");
        }
        if (!application.getUser().getId().equals(userId)) {
            throw new ApiException("This application does not belong to you");
        }

        String interviewsInfo = "No interviews yet";
        Set<Interview> interviews = application.getInterviews();
        if (interviews != null && !interviews.isEmpty()) {
            List<String> statuses = new ArrayList<>();
            for (Interview interview : interviews) {
                statuses.add(interview.getStatus() != null ? interview.getStatus() : "Unknown");
            }
            interviewsInfo = interviews.size() + " interview(s) with statuses: " + String.join(", ", statuses);
        }

        String prompt = String.format("""
            Act as an expert career coach.
            Suggest the single best next step for a candidate based on their job application.

            Company: %s
            Position: %s
            Application status: %s
            Interviews: %s

            Respond in English.
            Strictly return ONLY a valid JSON object matching this exact schema (no markdown formatting, no text outside JSON):
            {
              "nextStep": "One clear, actionable next step for the candidate",
              "reason": "Short explanation of why this is the best next step"
            }
            """,
                application.getJob().getCompany().getNameEn(),
                application.getJob().getPosition(),
                application.getStatus(),
                interviewsInfo
        );

        String rawAiOutput = geminiService.generateText(prompt);

        try {
            String cleanJson = rawAiOutput.replaceAll("```json|```", "").trim();
            return objectMapper.readValue(cleanJson, NextStepResponse.class);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI generation failed. Please try again.");
        }
    }
}