package org.fadhel.tumoohplatform.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.fadhel.tumoohplatform.dto.in.LinkedInProfileRequest;
import org.fadhel.tumoohplatform.dto.out.LinkedInProfileResponse;
import org.fadhel.tumoohplatform.service.LinkedInProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai/linkedin")
@RequiredArgsConstructor
public class LinkedInProfileController {

    private final LinkedInProfileService linkedInProfileService;

    @PostMapping
    public ResponseEntity<LinkedInProfileResponse> generateLinkedInProfile(
            @Valid @RequestBody LinkedInProfileRequest request) {
        LinkedInProfileResponse response = linkedInProfileService.generateLinkedInProfile(request);
        return ResponseEntity.ok(response);
    }

}
