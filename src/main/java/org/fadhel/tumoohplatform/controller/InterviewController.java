package org.fadhel.tumoohplatform.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.fadhel.tumoohplatform.Api.ApiResponse;
import org.fadhel.tumoohplatform.dto.in.InterviewRequest;
import org.fadhel.tumoohplatform.service.InterviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/interview")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;

    @GetMapping("/get")
    public ResponseEntity<?> getAllInterviews() {
        return ResponseEntity.status(200).body(interviewService.getAllInterviews());
    }

    @GetMapping("/get/user/{userId}")
    public ResponseEntity<?> getInterviewsByUserId(@PathVariable Long userId) {
        return ResponseEntity.status(200).body(interviewService.getInterviewsByUserId(userId));
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
}