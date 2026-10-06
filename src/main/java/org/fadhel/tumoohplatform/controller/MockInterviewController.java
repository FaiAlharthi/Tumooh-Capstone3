package org.fadhel.tumoohplatform.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.fadhel.tumoohplatform.Api.ApiResponse;
import org.fadhel.tumoohplatform.dto.in.MockInterviewRequest;
import org.fadhel.tumoohplatform.service.MockInterviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/mock-interviews")
@RequiredArgsConstructor
public class MockInterviewController {

    private final MockInterviewService mockInterviewService;

    @GetMapping("/get")
    public ResponseEntity<?> getAllMockInterviews() {
        return ResponseEntity.status(200).body(mockInterviewService.getAllMockInterviews());
    }

    @GetMapping("/get/user/{userId}")
    public ResponseEntity<?> getMockInterviewsByUserId(@PathVariable Long userId) {
        return ResponseEntity.status(200).body(mockInterviewService.getMockInterviewsByUserId(userId));
    }

    @PostMapping("/add")
    public ResponseEntity<?> createMockInterview(@RequestBody @Valid MockInterviewRequest mockInterviewRequest) {
        mockInterviewService.createMockInterview(mockInterviewRequest);
        return ResponseEntity.status(200).body(new ApiResponse("Mock interview added"));
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
}