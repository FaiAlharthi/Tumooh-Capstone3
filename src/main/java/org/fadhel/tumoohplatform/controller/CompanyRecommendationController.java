package org.fadhel.tumoohplatform.controller;

import lombok.RequiredArgsConstructor;
import org.fadhel.tumoohplatform.dto.out.CompanyRecommendationResponse;
import org.fadhel.tumoohplatform.service.CompanyRecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai/users")
@RequiredArgsConstructor
public class CompanyRecommendationController {

    private final CompanyRecommendationService companyRecommendationService;

    @PostMapping("/{userId}/company-recommendations")
    public ResponseEntity<CompanyRecommendationResponse> recommend(@PathVariable Long userId) {
        return ResponseEntity.ok(companyRecommendationService.getCompanyRecommendationsForUser(userId));
    }
}
