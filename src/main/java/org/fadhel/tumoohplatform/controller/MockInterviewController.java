package org.fadhel.tumoohplatform.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.fadhel.tumoohplatform.Api.ApiResponse;
import org.fadhel.tumoohplatform.dto.in.MockInterviewRequest;
import org.fadhel.tumoohplatform.dto.out.MockInterviewResponse;
import org.fadhel.tumoohplatform.dto.out.MockInterviewResultsResponse;
import org.fadhel.tumoohplatform.dto.out.MockInterviewStartResponse;
import org.fadhel.tumoohplatform.service.MockInterviewEvaluationService;
import org.fadhel.tumoohplatform.service.MockInterviewService;
import org.fadhel.tumoohplatform.service.MockInterviewSessionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/mock-interviews")
@RequiredArgsConstructor
@Slf4j
public class MockInterviewController {

    private final MockInterviewService mockInterviewService;
    private final MockInterviewSessionService mockInterviewSessionService;
    private final MockInterviewEvaluationService mockInterviewEvaluationService;

    @GetMapping("/get")
    public ResponseEntity<?> getAllMockInterviews() {
        return ResponseEntity.status(200).body(mockInterviewService.getAllMockInterviews());
    }

    @GetMapping("/get/user/{userId}")
    public ResponseEntity<java.util.List<MockInterviewResponse>> getMockInterviewsByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(mockInterviewService.getMockInterviewResponsesByUserId(userId));
    }

    @PostMapping("/add")
    public ResponseEntity<?> createMockInterview(@RequestBody @Valid MockInterviewRequest mockInterviewRequest) {
        Long id = mockInterviewService.createMockInterview(mockInterviewRequest);
        return ResponseEntity.status(200).body(new ApiResponse("Mock interview added with id " + id));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateMockInterviewResult(@PathVariable Long id, @RequestBody @Valid MockInterviewRequest mockInterviewRequest) {
        mockInterviewService.updateMockInterviewResult(id, mockInterviewRequest);
        return ResponseEntity.status(200).body(new ApiResponse("Mock interview updated"));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteMockInterview(@PathVariable Long id) {
        mockInterviewService.deleteMockInterview(id);
        return ResponseEntity.status(200).body(new ApiResponse("Mock interview deleted"));
    }

    @PostMapping("/start")
    public ResponseEntity<MockInterviewStartResponse> startMockInterview(
            @RequestBody @Valid MockInterviewRequest mockInterviewRequest) {
        return ResponseEntity.ok(mockInterviewSessionService.startMockInterview(mockInterviewRequest));
    }

    @PostMapping(value = "/{id}/telemetry", consumes = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<ApiResponse> saveTelemetry(@PathVariable Long id, @RequestBody String telemetry) {
        mockInterviewSessionService.saveSessionTelemetry(id, telemetry);
        return ResponseEntity.ok(new ApiResponse("Telemetry saved"));
    }

    @PostMapping("/{id}/evaluate")
    public ResponseEntity<MockInterviewResultsResponse> evaluate(@PathVariable Long id) {
        boolean alreadyEvaluated = mockInterviewService.getMockInterviewById(id).getAiScore() != null;
        String telemetry = mockInterviewSessionService.getSessionTelemetry(id);
        MockInterviewEvaluationService.EvaluationResult result =
                mockInterviewEvaluationService.evaluateIfNeeded(id, telemetry);
        MockInterviewResultsResponse body = mockInterviewService.getMockInterviewResults(id);
        if (result.outcome() != MockInterviewEvaluationService.EvaluationOutcome.SUCCESS
                || !body.isEvaluationAvailable()) {
            body.setEvaluationAvailable(false);
            body.setMessage(result.message() != null ? result.message() : body.getMessage());
        } else if (!alreadyEvaluated) {
            try {
                mockInterviewService.sendMockInterviewFeedbackEmail(id);
            } catch (Exception e) {
                log.warn("Mock interview {} evaluated but feedback email failed: {}", id, e.getMessage());
                body.setMessage("Evaluation saved. Feedback email could not be sent: " + e.getMessage());
            }
        }
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{id}/results")
    public ResponseEntity<MockInterviewResultsResponse> results(@PathVariable Long id) {
        return ResponseEntity.ok(mockInterviewService.getMockInterviewResults(id));
    }

    @PostMapping("/{id}/email-feedback")
    public ResponseEntity<ApiResponse> emailFeedback(@PathVariable Long id) {
        mockInterviewService.sendMockInterviewFeedbackEmail(id);
        return ResponseEntity.ok(new ApiResponse("Feedback emailed to user"));
    }
}