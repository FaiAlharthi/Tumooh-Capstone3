package org.fadhel.tumoohplatform.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.fadhel.tumoohplatform.dto.in.StarAnswerRequest;
import org.fadhel.tumoohplatform.dto.out.StarAnswerResponse;
import org.fadhel.tumoohplatform.service.StarAnswerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai/star-answer")
@RequiredArgsConstructor
public class StarAnswerController {

    private final StarAnswerService starAnswerService;

    @PostMapping
    public ResponseEntity<StarAnswerResponse> buildStarAnswer(
            @Valid @RequestBody StarAnswerRequest request) {
        StarAnswerResponse response = starAnswerService.buildStarAnswer(request);
        return ResponseEntity.ok(response);
    }

}