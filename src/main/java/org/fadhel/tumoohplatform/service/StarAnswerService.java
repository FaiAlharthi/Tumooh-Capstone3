package org.fadhel.tumoohplatform.service;


import lombok.RequiredArgsConstructor;
import org.fadhel.tumoohplatform.dto.in.StarAnswerRequest;
import org.fadhel.tumoohplatform.dto.out.StarAnswerResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class StarAnswerService {

    private final GeminiService geminiService;
    private final ObjectMapper objectMapper;

    public StarAnswerResponse buildStarAnswer(StarAnswerRequest request) {
        String prompt = String.format("""
            Act as an expert interview coach and STAR method specialist.
            Rebuild the candidate's raw story into a strong STAR answer for the behavioral question:
            '%s'

            Target role: %s

            Desired tone: %s

            Candidate's raw story:
            %s

            Rules: keep the answer faithful to the candidate's story - never invent achievements, people or
            numbers. Where a metric is missing, phrase it credibly (e.g. quantify with realistic framing or say
            "led the effort"). Return the full answer and each STAR section separately.

            Strictly return ONLY a valid JSON object matching this exact schema (no markdown formatting, no text outside JSON):
            {
              "starAnswer": "Full polished STAR answer that flows naturally as one response.",
              "situation": "Situation section text",
              "task": "Task section text",
              "action": "Action section text - the strongest, most detailed part",
              "result": "Result section text including any outcome",
              "strengthsRevealed": ["Competency the story demonstrates 1", "Competency 2"],
              "whatToPolish": ["Weak point or missing detail in the current story 1", "Weak point 2"],
              "tips": ["Delivery tip 1 for interviews", "Delivery tip 2"]
            }
            """,
                request.getQuestion(),
                (request.getTargetRole() != null ? request.getTargetRole() : "General behavioral interview"),
                (request.getTone() != null ? request.getTone() : "professional and confident"),
                request.getRawStory()
        );

        String rawAiOutput = geminiService.generateText(prompt);

        try {
            String cleanJson = rawAiOutput.replaceAll("```json|```", "").trim();

            StarAnswerResponse response = objectMapper.readValue(cleanJson, StarAnswerResponse.class);
            response.setQuestion(request.getQuestion());
            return response;

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "AI generation failed. Please try again.");
        }
    }

}