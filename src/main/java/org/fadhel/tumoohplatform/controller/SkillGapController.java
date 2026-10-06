package org.fadhel.tumoohplatform.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.fadhel.tumoohplatform.dto.in.SkillGapRequest;
import org.fadhel.tumoohplatform.dto.out.SkillGapResponse;
import org.fadhel.tumoohplatform.service.SkillGapService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai/skill-gap")
@RequiredArgsConstructor
public class SkillGapController {

    private final SkillGapService skillGapService;

    @PostMapping
    public ResponseEntity<SkillGapResponse> analyzeSkillGap(
            @Valid @RequestBody SkillGapRequest request) {
        SkillGapResponse response = skillGapService.analyzeSkillGap(request);
        return ResponseEntity.ok(response);
    }

}
