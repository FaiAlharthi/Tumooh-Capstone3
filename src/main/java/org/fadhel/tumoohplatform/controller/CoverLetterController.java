package org.fadhel.tumoohplatform.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.fadhel.tumoohplatform.dto.in.CoverLetterRequest;
import org.fadhel.tumoohplatform.dto.out.CoverLetterResponse;
import org.fadhel.tumoohplatform.service.CoverLetterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai/cover-letter")
@RequiredArgsConstructor
public class CoverLetterController {

    private final CoverLetterService coverLetterService;

    @PostMapping
    public ResponseEntity<CoverLetterResponse> generateCoverLetter(
            @Valid @RequestBody CoverLetterRequest request) {
        CoverLetterResponse response = coverLetterService.generateCoverLetter(request);
        return ResponseEntity.ok(response);
    }

}
