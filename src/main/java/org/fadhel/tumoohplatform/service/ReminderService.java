package org.fadhel.tumoohplatform.service;

import lombok.RequiredArgsConstructor;
import org.fadhel.tumoohplatform.Api.ApiException;
import org.fadhel.tumoohplatform.dto.in.ReminderRequest;
import org.fadhel.tumoohplatform.dto.out.ReminderResponse;
import org.fadhel.tumoohplatform.model.JobApplication;
import org.fadhel.tumoohplatform.model.Reminder;
import org.fadhel.tumoohplatform.repository.JobApplicationRepository;
import org.fadhel.tumoohplatform.repository.ReminderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReminderService {

    private final ReminderRepository reminderRepository;
    private final JobApplicationRepository jobApplicationRepository;

    @Transactional
    public ReminderResponse createReminder(ReminderRequest request) {
        JobApplication jobApplication = jobApplicationRepository.findById(request.getJobApplicationId())
                .orElseThrow(() -> new ApiException("Job Application not found with id: " + request.getJobApplicationId()));

        Reminder reminder = Reminder.builder()
                .jobApplication(jobApplication)
                .reminderLetter(request.getReminderLetter())
                .reminderDate(request.getReminderDate())
                .isSent(request.getIsSent())
                .build();

        Reminder saved = reminderRepository.save(reminder);
        return mapToResponse(saved);
    }

    public ReminderResponse getReminderById(Long id) {
        Reminder reminder = reminderRepository.findById(id)
                .orElseThrow(() -> new ApiException("Reminder not found with id: " + id));
        return mapToResponse(reminder);
    }

    public List<ReminderResponse> getRemindersByJobApplicationId(Long jobApplicationId) {
        return reminderRepository.findByJobApplicationId(jobApplicationId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ReminderResponse> getAllReminders() {
        return reminderRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ReminderResponse updateReminder(Long id, ReminderRequest request) {
        Reminder reminder = reminderRepository.findById(id)
                .orElseThrow(() -> new ApiException("Reminder not found with id: " + id));

        if (!reminder.getJobApplication().getId().equals(request.getJobApplicationId())) {
            JobApplication newJobApplication = jobApplicationRepository.findById(request.getJobApplicationId())
                    .orElseThrow(() -> new ApiException("Job Application not found with id: " + request.getJobApplicationId()));
            reminder.setJobApplication(newJobApplication);
        }

        reminder.setReminderLetter(request.getReminderLetter());
        reminder.setReminderDate(request.getReminderDate());
        reminder.setIsSent(request.getIsSent());

        Reminder updated = reminderRepository.save(reminder);
        return mapToResponse(updated);
    }

    public void deleteReminder(Long id) {
        Reminder reminder = reminderRepository.findById(id)
                .orElseThrow(() -> new ApiException("Reminder not found with id: " + id));
        reminderRepository.delete(reminder);
    }

    private ReminderResponse mapToResponse(Reminder reminder) {
        return ReminderResponse.builder()
                .id(reminder.getId())
                .jobApplicationId(reminder.getJobApplication() != null ? reminder.getJobApplication().getId() : null)
                .reminderLetter(reminder.getReminderLetter())
                .reminderDate(reminder.getReminderDate())
                .isSent(reminder.getIsSent())
                .build();
    }

}
