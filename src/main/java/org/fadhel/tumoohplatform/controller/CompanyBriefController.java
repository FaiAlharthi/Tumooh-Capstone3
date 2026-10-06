package org.fadhel.tumoohplatform.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.fadhel.tumoohplatform.dto.in.CompanyBriefRequest;
import org.fadhel.tumoohplatform.dto.out.CompanyBriefResponse;
import org.fadhel.tumoohplatform.service.CompanyBriefService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai/company-brief")
@RequiredArgsConstructor
public class CompanyBriefController {

    private final CompanyBriefService companyBriefService;

    @PostMapping
    public ResponseEntity<CompanyBriefResponse> generateCompanyBrief(
            @Valid @RequestBody CompanyBriefRequest request) {
        CompanyBriefResponse response = companyBriefService.generateCompanyBrief(request);
        return ResponseEntity.ok(response);
    }

}