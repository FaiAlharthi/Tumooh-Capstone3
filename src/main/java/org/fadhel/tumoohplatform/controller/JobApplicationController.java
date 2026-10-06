package org.fadhel.tumoohplatform.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.fadhel.tumoohplatform.Api.ApiResponse;
import org.fadhel.tumoohplatform.dto.in.ApplicationStatusRequest;
import org.fadhel.tumoohplatform.dto.in.DatesRequest;
import org.fadhel.tumoohplatform.dto.in.ManualApplicationRequest;
import org.fadhel.tumoohplatform.model.JobApplication;
import org.fadhel.tumoohplatform.service.InsightsService;
import org.fadhel.tumoohplatform.service.JobApplicationService;
import org.fadhel.tumoohplatform.service.ReplyMessageService;
import org.fadhel.tumoohplatform.service.suggestNextStepService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/job-applications")
@RequiredArgsConstructor
public class JobApplicationController {

    private final JobApplicationService jobApplicationService;
    private final suggestNextStepService nextStepService;
    private final InsightsService insightsService;
    private final ReplyMessageService replyMessageService;

    @GetMapping("/get")
    public ResponseEntity<?> getAllJobApplications() {
        return ResponseEntity.status(200).body(jobApplicationService.getAllJobApplications());
    }

    @PostMapping("/add/{userId}/{jobId}")
    public ResponseEntity<?> addJobApplication(@PathVariable Long userId, @PathVariable Long jobId,
                                               @RequestBody @Valid JobApplication jobApplication) {
        jobApplicationService.addJobApplication(userId, jobId, jobApplication);
        return ResponseEntity.status(200).body(new ApiResponse("Job application added"));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateJobApplication(@PathVariable Long id, @RequestBody @Valid JobApplication jobApplication) {
        jobApplicationService.updateJobApplication(id, jobApplication);
        return ResponseEntity.status(200).body(new ApiResponse("Job application updated"));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteJobApplication(@PathVariable Long id) {
        jobApplicationService.deleteJobApplication(id);
        return ResponseEntity.status(200).body(new ApiResponse("Job application deleted"));
    }

    @PutMapping("/update-status/{userId}/{applicationId}")
    public ResponseEntity<?> updateApplicationStatus(@PathVariable Long userId, @PathVariable Long applicationId, @RequestBody @Valid ApplicationStatusRequest statusDto) {
        jobApplicationService.updateApplicationStatus(userId, applicationId, statusDto);
        return ResponseEntity.status(200).body(new ApiResponse("Job application status updated"));
    }

    @PostMapping("/add-manual/{userId}")
    public ResponseEntity<?> addManualApplication(@PathVariable Long userId, @RequestBody @Valid ManualApplicationRequest application) {
        jobApplicationService.addManualApplication(userId, application);
        return ResponseEntity.status(200).body(new ApiResponse("Job application added"));
    }

    @GetMapping("/status/{userId}")
    public ResponseEntity<?> getApplicationsByStatus(@PathVariable Long userId, @RequestBody @Valid ApplicationStatusRequest status) {
        return ResponseEntity.status(200).body(jobApplicationService.getApplicationsByStatus(userId, status));
    }

    @GetMapping("/history/{userId}/{companyName}")
    public ResponseEntity<?> applicationsHistoryByCompany(@PathVariable Long userId, @PathVariable String companyName) {
        return ResponseEntity.status(200).body(jobApplicationService.applicationsHistoryByCompany(userId, companyName));
    }

    @GetMapping("/period/{userId}")
    public ResponseEntity<?> periodApplications(@PathVariable Long userId, @RequestBody @Valid DatesRequest period) {
        return ResponseEntity.status(200).body(jobApplicationService.periodApplications(userId, period));
    }

    @GetMapping("/interviews/{userId}/{applicationId}")
    public ResponseEntity<?> allApplicationInterviews(@PathVariable Long userId, @PathVariable Long applicationId) {
        return ResponseEntity.status(200).body(jobApplicationService.allApplicationInterviews(userId, applicationId));
    }

    @GetMapping("/next-step/{userId}/{applicationId}")
    public ResponseEntity<?> suggestNextStep(@PathVariable Long userId, @PathVariable Long applicationId) {
        return ResponseEntity.status(200).body(nextStepService.suggestNextStep(userId, applicationId));
    }

    @GetMapping("/insights/{userId}")
    public ResponseEntity<?> getInsights(@PathVariable Long userId) {
        return ResponseEntity.status(200).body(insightsService.getInsights(userId));
    }

    @GetMapping("/reply/{userId}/{applicationId}")
    public ResponseEntity<?> writeReply(@PathVariable Long userId, @PathVariable Long applicationId) {
        return ResponseEntity.status(200).body(replyMessageService.writeReply(userId, applicationId));
    }



}