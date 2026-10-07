package org.fadhel.tumoohplatform.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fadhel.tumoohplatform.dto.out.MockInterviewEvaluationResponse;
import org.fadhel.tumoohplatform.model.MockInterview;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class MockInterviewEvaluationService {

    private final GeminiService geminiService;
    private final MockInterviewService mockInterviewService;
    private final MockInterviewSessionService mockInterviewSessionService;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key:}")
    private String apiKey;

    public enum EvaluationOutcome {
        SUCCESS,
        NO_API_KEY,
        NO_TELEMETRY,
        FAILED
    }

    public record EvaluationResult(
            EvaluationOutcome outcome,
            MockInterview mockInterview,
            String message,
            Double speechClarity) {}

    public EvaluationResult evaluateIfNeeded(Long mockInterviewId, String sessionNotes) {
        MockInterview mockInterview = mockInterviewService.getMockInterviewById(mockInterviewId);

        if (mockInterview.getAiScore() != null) {
            return new EvaluationResult(
                    EvaluationOutcome.SUCCESS,
                    mockInterview,
                    null,
                    mockInterview.getSpeechClarity());
        }

        if (apiKey == null || apiKey.isBlank() || apiKey.startsWith("${")) {
            return new EvaluationResult(
                    EvaluationOutcome.NO_API_KEY,
                    mockInterview,
                    "Add a Gemini API key in application.properties to enable AI feedback.",
                    null);
        }

        String notes = sessionNotes != null ? sessionNotes.trim() : "";
        if (notes.isBlank()) {
            return new EvaluationResult(
                    EvaluationOutcome.NO_TELEMETRY,
                    mockInterview,
                    "No session data was captured. Allow camera and microphone, complete the full timer, then try again.",
                    null);
        }

        if (!notes.contains("CAMERA_ACTIVE:true")) {
            return new EvaluationResult(
                    EvaluationOutcome.NO_TELEMETRY,
                    mockInterview,
                    "This session did not include an active camera feed. Enable camera access and run the room again.",
                    null);
        }

        String questionsBlock = formatQuestionsForPrompt(mockInterviewSessionService.getSessionQuestions(mockInterview));

        String prompt = String.format("""
            Act as an expert interview coach. Evaluate a mock interview for the role '%s'.
            
            Questions the candidate was asked to answer aloud:
            %s
            
            Session telemetry captured from the browser (ONLY use these facts — do not invent speech, video, or answers):
            %s
            
            Rules:
            - Grade how well the transcript addresses the listed questions (content relevance, structure, clarity).
            - If NO_SPEECH_DETECTED is true or transcript is empty with negligible speech time, \
            aiScore and speechClarity MUST be between 0 and 25.
            - Never praise pacing, engagement, or answers unless the telemetry shows speech and content.
            - Score based on what was actually said, not what an ideal candidate would say.
            - strengths and weaknesses may reference answer content; keep them free of raw telemetry jargon.
            
            bodyLanguageTips (important):
            - Write directly to the candidate in second person ("you"), warm and practical, like a coach after a video interview.
            - Focus on on-camera presence: eye contact with the lens, framing/head position, posture, visible confidence, \
            clear voice projection, pacing, and calm energy.
            - Use plain language a candidate expects on a feedback page. Never mention telemetry, sensors, transcripts, \
            peak microphone level, numeric audio values, NO_SPEECH_DETECTED, CAMERA_ACTIVE, or session duration seconds.
            - Do not claim you observed specific gestures or room details; give actionable video-interview habits \
            (infer gently from speech clarity/volume in the data without citing numbers).
            - If they barely spoke, encourage showing up on camera next time and simple habits (sit square to camera, \
            look at the lens when introducing yourself).
            
            Strictly return ONLY a valid JSON object (no markdown, no extra text):
            {
              "aiScore": 85.5,
              "speechClarity": 78.0,
              "strengths": "Multi-sentence constructive strengths.",
              "weaknesses": "Multi-sentence areas to improve.",
              "bodyLanguageTips": "Multi-sentence coaching tips for camera presence and delivery."
            }
            
            aiScore and speechClarity are numbers from 0 to 100.
            """, mockInterview.getJobTitle(), questionsBlock, notes);

        try {
            String rawAiOutput = generateEvaluationWithRetries(prompt);
            String cleanJson = rawAiOutput.replaceAll("```json|```", "").trim();
            int start = cleanJson.indexOf('{');
            int end = cleanJson.lastIndexOf('}');
            if (start >= 0 && end > start) {
                cleanJson = cleanJson.substring(start, end + 1);
            }
            MockInterviewEvaluationResponse dto = objectMapper.readValue(cleanJson, MockInterviewEvaluationResponse.class);
            clampIfSilentNotes(notes, dto);

            mockInterviewService.saveMockInterviewEvaluation(mockInterviewId, dto);
            MockInterview updated = mockInterviewService.getMockInterviewById(mockInterviewId);
            return new EvaluationResult(EvaluationOutcome.SUCCESS, updated, null, dto.getSpeechClarity());
        } catch (Exception e) {
            log.error("Mock interview evaluation failed for id {}", mockInterviewId, e);
            return new EvaluationResult(
                    EvaluationOutcome.FAILED,
                    mockInterview,
                    "Could not complete AI evaluation. Check your Gemini API key, model, and network, then try again.",
                    null);
        }
    }

    private String generateEvaluationWithRetries(String prompt) {
        int attempts = 4;
        for (int attempt = 1; attempt <= attempts; attempt++) {
            try {
                return geminiService.generateText(prompt);
            } catch (Exception e) {
                if (!isTransientGeminiError(e) || attempt == attempts) {
                    throw e;
                }
                try {
                    Thread.sleep(1500L * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw e;
                }
            }
        }
        throw new IllegalStateException("Gemini evaluation retries exhausted");
    }

    private static boolean isTransientGeminiError(Throwable e) {
        String msg = e.getMessage() != null ? e.getMessage() : "";
        if (msg.contains("429")
                || msg.contains("Too Many Requests")
                || msg.contains("RESOURCE_EXHAUSTED")
                || msg.contains("quota")) {
            return false;
        }
        return msg.contains("503")
                || msg.contains("UNAVAILABLE")
                || msg.contains("high demand");
    }

    private static boolean isSilentSession(String notes) {
        return notes.contains("NO_SPEECH_DETECTED:true");
    }

    private static String formatQuestionsForPrompt(java.util.List<String> questions) {
        if (questions == null || questions.isEmpty()) {
            return "(No questions stored for this session.)";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < questions.size(); i++) {
            sb.append(i + 1).append(". ").append(questions.get(i)).append("\n");
        }
        return sb.toString();
    }

    private static void clampIfSilentNotes(String notes, MockInterviewEvaluationResponse dto) {
        if (!isSilentSession(notes)) {
            return;
        }
        dto.setAiScore(Math.min(dto.getAiScore() != null ? dto.getAiScore() : 25, 25));
        dto.setSpeechClarity(Math.min(dto.getSpeechClarity() != null ? dto.getSpeechClarity() : 25, 25));
    }
}