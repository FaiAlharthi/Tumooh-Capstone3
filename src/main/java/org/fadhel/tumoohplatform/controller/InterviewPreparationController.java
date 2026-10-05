package org.fadhel.tumoohplatform.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.fadhel.tumoohplatform.dto.in.InterviewPreparationRequest;
import org.fadhel.tumoohplatform.dto.out.InterviewPreparationResponse;
import org.fadhel.tumoohplatform.service.InterviewPreparationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai/interview-prep")
@RequiredArgsConstructor
public class InterviewPreparationController {

    private final InterviewPreparationService interviewPreparationService;

    @PostMapping
    public ResponseEntity<InterviewPreparationResponse> getPreparationSuggestions(
            @Valid @RequestBody InterviewPreparationRequest request) {
        InterviewPreparationResponse response = interviewPreparationService.generatePreparationGuide(request);
        return ResponseEntity.ok(response);
    }

}
