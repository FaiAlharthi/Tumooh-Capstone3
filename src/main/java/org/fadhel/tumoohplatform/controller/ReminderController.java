package org.fadhel.tumoohplatform.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.fadhel.tumoohplatform.Api.ApiResponse;
import org.fadhel.tumoohplatform.dto.in.ReminderRequest;
import org.fadhel.tumoohplatform.dto.out.ReminderResponse;
import org.fadhel.tumoohplatform.service.ReminderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reminders")
@RequiredArgsConstructor
public class ReminderController {

    private final ReminderService reminderService;

    @PostMapping
    public ResponseEntity<ReminderResponse> createReminder(@Valid @RequestBody ReminderRequest request) {
        ReminderResponse created = reminderService.createReminder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReminderResponse> getReminderById(@PathVariable Long id) {
        ReminderResponse reminder = reminderService.getReminderById(id);
        return ResponseEntity.ok(reminder);
    }

    @GetMapping("/job-application/{jobApplicationId}")
    public ResponseEntity<List<ReminderResponse>> getRemindersByJobApplicationId(@PathVariable Long jobApplicationId) {
        List<ReminderResponse> reminders = reminderService.getRemindersByJobApplicationId(jobApplicationId);
        return ResponseEntity.ok(reminders);
    }

    @GetMapping
    public ResponseEntity<List<ReminderResponse>> getAllReminders() {
        List<ReminderResponse> reminders = reminderService.getAllReminders();
        return ResponseEntity.ok(reminders);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReminderResponse> updateReminder(
            @PathVariable Long id,
            @Valid @RequestBody ReminderRequest request) {
        ReminderResponse updated = reminderService.updateReminder(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteReminder(@PathVariable Long id) {
        reminderService.deleteReminder(id);
        return ResponseEntity.status(200).body(new ApiResponse("Reminder Deleted Successfully"));
    }
}
