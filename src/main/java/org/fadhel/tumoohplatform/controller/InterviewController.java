package org.fadhel.tumoohplatform.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.fadhel.tumoohplatform.Api.ApiResponse;
import org.fadhel.tumoohplatform.dto.in.InterviewRequest;
import org.fadhel.tumoohplatform.dto.in.InterviewStatusPatchRequest;
import org.fadhel.tumoohplatform.dto.out.InterviewResponse;
import org.fadhel.tumoohplatform.service.InterviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/interviews")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;

    @GetMapping("/get")
    public ResponseEntity<List<InterviewResponse>> getAllInterviews() {
        return ResponseEntity.ok(interviewService.getAllInterviews());
    }

    @GetMapping("/get/user/{userId}")
    public ResponseEntity<List<InterviewResponse>> getInterviewsByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(interviewService.getInterviewsByUserId(userId));
    }

    @PostMapping("/add")
    public ResponseEntity<?> createInterview(@RequestBody @Valid InterviewRequest interviewRequest) {
        interviewService.createInterview(interviewRequest);
        return ResponseEntity.status(200).body(new ApiResponse("Interview added"));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateInterview(@PathVariable Long id, @RequestBody @Valid InterviewRequest interviewRequest) {
        interviewService.updateInterview(id, interviewRequest);
        return ResponseEntity.status(200).body(new ApiResponse("Interview updated"));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteInterview(@PathVariable Long id) {
        interviewService.deleteInterview(id);
        return ResponseEntity.status(200).body(new ApiResponse("Interview deleted"));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse> patchStatus(
            @PathVariable Long id,
            @RequestBody @Valid InterviewStatusPatchRequest request) {
        interviewService.updateInterviewStatus(id, request.getStatus());
        return ResponseEntity.ok(new ApiResponse("Interview status updated"));
    }

    @PostMapping("/{id}/send-reminder")
    public ResponseEntity<ApiResponse> sendReminder(@PathVariable Long id) {
        interviewService.sendInterviewReminderEmail(id);
        return ResponseEntity.ok(new ApiResponse("Reminder email sent"));
    }
}