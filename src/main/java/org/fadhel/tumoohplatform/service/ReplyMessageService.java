package org.fadhel.tumoohplatform.service;

import lombok.RequiredArgsConstructor;
import org.fadhel.tumoohplatform.Api.ApiException;
import org.fadhel.tumoohplatform.dto.out.ReplyResponse;
import org.fadhel.tumoohplatform.model.JobApplication;
import org.fadhel.tumoohplatform.model.Profile;
import org.fadhel.tumoohplatform.model.User;
import org.fadhel.tumoohplatform.repository.JobApplicationRepository;
import org.fadhel.tumoohplatform.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReplyMessageService {

    private final UserRepository userRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final GeminiService geminiService;
    private final ObjectMapper objectMapper;

    public List<ReplyResponse> writeReply(Long userId, Long applicationId) {
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
        String status = application.getStatus();
        String replyInstructions;
        if (status.equals("Offered")) {
            replyInstructions = """
                The candidate received a job offer. Write TWO different replies:
                1) A reply accepting the offer with enthusiasm and gratitude.
                2) A reply thanking them for the offer and politely asking for some time to consider it.
                """;
        } else if (status.equals("Rejected")) {
            replyInstructions = """
                The candidate was rejected. Write ONE reply:
                A polite reply thanking them for the opportunity and asking them to keep the candidate in mind for future opportunities.
                """;
        } else {
            throw new ApiException("Application status does not require writing an email");
        }

        Profile profile = user.getProfile();
        String seekerName = "Sender name";
        if (profile != null) {
            seekerName = profile.getFullName();
        }

        String prompt = String.format("""
            Act as an expert career coach and professional email writer.
            Write a professional email reply from a candidate to a company.

            Company: %s
            Position: %s
            Candidate name (use it to sign the email): %s

            %s

            Respond in English.
            Strictly return ONLY a valid JSON array matching this exact schema (no markdown formatting, no text outside JSON):
            [
              {
                "subject": "Email subject line",
                "reply": "The complete email text with greeting, body and closing signed with the candidate name"
              }
            ]
            """,
                application.getJob().getCompany().getNameEn(),
                application.getJob().getPosition(), seekerName, replyInstructions
        );

        String rawAiOutput = geminiService.generateText(prompt);

        try {
            String cleanJson = rawAiOutput.replaceAll("```json|```", "").trim();
            return objectMapper.readValue(cleanJson, new TypeReference<List<ReplyResponse>>() {});
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI generation failed. Please try again.");
        }
    }
}