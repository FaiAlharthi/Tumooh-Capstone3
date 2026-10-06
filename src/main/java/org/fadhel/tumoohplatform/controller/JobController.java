package org.fadhel.tumoohplatform.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.fadhel.tumoohplatform.Api.ApiResponse;
import org.fadhel.tumoohplatform.model.Job;
import org.fadhel.tumoohplatform.service.JobService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
public class JobController {
    private final JobService jobService;

    @GetMapping("/get")
    public ResponseEntity<?> getAllJobs() {
        return ResponseEntity.status(200).body(jobService.getAllJobs());
    }

    @PostMapping("/add/{companyId}")
    public ResponseEntity<?> addJob(@PathVariable Long companyId, @RequestBody @Valid Job job) {
        jobService.addJob(companyId, job);
        return ResponseEntity.status(200).body(new ApiResponse("Job added"));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateJob(@PathVariable Long id, @RequestBody @Valid Job job) {
        jobService.updateJob(id, job);
        return ResponseEntity.status(200).body(new ApiResponse("Job updated"));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteJob(@PathVariable Long id) {
        jobService.deleteJob(id);
        return ResponseEntity.status(200).body(new ApiResponse("Job deleted"));
    }
}
