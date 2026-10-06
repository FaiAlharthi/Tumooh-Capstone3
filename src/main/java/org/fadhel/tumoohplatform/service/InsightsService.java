package org.fadhel.tumoohplatform.service;

import lombok.RequiredArgsConstructor;
import org.fadhel.tumoohplatform.Api.ApiException;
import org.fadhel.tumoohplatform.dto.out.InsightsResponse;
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

@Service
@RequiredArgsConstructor
public class InsightsService {

    private final UserRepository userRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final GeminiService geminiService;
    private final ObjectMapper objectMapper;

    public InsightsResponse getInsights(Long userId) {
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ApiException("User not found");
        }
        List<JobApplication> applications = jobApplicationRepository.findJobApplicationsByUser(user);
        if (applications.isEmpty()) {
            throw new ApiException("You have no applications yet");
        }

        int appliedCount = 0;
        int inProgressCount = 0;
        int offeredCount = 0;
        int rejectedCount = 0;
        int withdrawnCount = 0;
        int totalInterviews = 0;
        List<String> industries = new ArrayList<>();
        List<String> applicationsList = new ArrayList<>();

        for (JobApplication application : applications) {
            String status = application.getStatus();
            if (status.equals("Applied")) {
                appliedCount++;
            } else if (status.equals("InProgress")) {
                inProgressCount++;
            } else if (status.equals("Offered")) {
                offeredCount++;
            } else if (status.equals("Rejected")) {
                rejectedCount++;
            } else if (status.equals("Withdrawn")) {
                withdrawnCount++;
            }

            String industry = application.getJob().getCompany().getIndustryEn();
            if (industry == null || industry.isBlank()) {
                industry = "Other";
            }
            if (!industries.contains(industry)) {
                industries.add(industry);
            }

            if (application.getInterviews() != null) {
                totalInterviews += application.getInterviews().size();
            }

            applicationsList.add("- " + application.getJob().getPosition()
                    + " at " + application.getJob().getCompany().getNameEn()
                    + " (" + status + ")");
        }

// send results to gemini
        String prompt = String.format("""
            Act as an expert career coach.
            Analyze the job search of a candidate based on these statistics and give insights.

            Total applications: %d
            Applied: %d
            In progress: %d
            Offered: %d
            Rejected: %d
            Withdrawn: %d
            Industries applied to: %s
            Total interviews: %d

            Applications:
            %s

            Respond in English.
            Strictly return ONLY a valid JSON object matching this exact schema (no markdown formatting, no text outside JSON):
            {
              "strengths": ["What the candidate is doing well 1", "Strength 2"],
              "concerns": ["A pattern or problem in the job search 1", "Concern 2"],
              "recommendations": ["Concrete actionable recommendation 1", "Recommendation 2"]
            }
            """,
                applications.size(),
                appliedCount,
                inProgressCount,
                offeredCount,
                rejectedCount,
                withdrawnCount,
                String.join(", ", industries),
                totalInterviews,
                String.join("\n", applicationsList)
        );

        String rawAiOutput = geminiService.generateText(prompt);


        try {
            String cleanJson = rawAiOutput.replaceAll("```json|```", "").trim();
            InsightsResponse response = objectMapper.readValue(cleanJson, InsightsResponse.class);

            response.setTotalApplications(applications.size());
            response.setAppliedCount(appliedCount);
            response.setInProgressCount(inProgressCount);
            response.setOfferedCount(offeredCount);
            response.setRejectedCount(rejectedCount);
            response.setWithdrawnCount(withdrawnCount);
            response.setIndustries(industries);
            response.setTotalInterviews(totalInterviews);
            return response;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI generation failed. Please try again.");
        }
    }
}