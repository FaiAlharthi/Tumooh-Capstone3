package org.fadhel.tumoohplatform.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.fadhel.tumoohplatform.dto.in.CareerPivotRequest;
import org.fadhel.tumoohplatform.dto.out.CareerPivotResponse;
import org.fadhel.tumoohplatform.service.CareerPivotService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai/career-pivot")
@RequiredArgsConstructor
public class CareerPivotController {

    private final CareerPivotService careerPivotService;

    @PostMapping
    public ResponseEntity<CareerPivotResponse> analyzeCareerPivot(
            @Valid @RequestBody CareerPivotRequest request) {
        CareerPivotResponse response = careerPivotService.analyzeCareerPivot(request);
        return ResponseEntity.ok(response);
    }

}