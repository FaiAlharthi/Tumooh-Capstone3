package org.fadhel.tumoohplatform.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.fadhel.tumoohplatform.dto.in.SalaryBenchmarkRequest;
import org.fadhel.tumoohplatform.dto.out.SalaryBenchmarkResponse;
import org.fadhel.tumoohplatform.service.SalaryBenchmarkService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai/salary-benchmark")
@RequiredArgsConstructor
public class SalaryBenchmarkController {

    private final SalaryBenchmarkService salaryBenchmarkService;

    @PostMapping
    public ResponseEntity<SalaryBenchmarkResponse> benchmarkSalary(
            @Valid @RequestBody SalaryBenchmarkRequest request) {
        SalaryBenchmarkResponse response = salaryBenchmarkService.benchmarkSalary(request);
        return ResponseEntity.ok(response);
    }

}
