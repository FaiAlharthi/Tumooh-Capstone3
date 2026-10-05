package org.fadhel.tumoohplatform.service;

import lombok.RequiredArgsConstructor;
import org.fadhel.tumoohplatform.Api.ApiException;
import org.fadhel.tumoohplatform.dto.in.InterviewRequest;
import org.fadhel.tumoohplatform.model.Interview;
import org.fadhel.tumoohplatform.model.JobApplication;
import org.fadhel.tumoohplatform.model.User;
import org.fadhel.tumoohplatform.repository.InterviewRepository;
import org.fadhel.tumoohplatform.repository.JobApplicationRepository;
import org.fadhel.tumoohplatform.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InterviewService {

    private final InterviewRepository interviewRepository;
    private final UserRepository userRepository;
    private final JobApplicationRepository jobApplicationRepository;

    public List<Interview> getAllInterviews() {
        return interviewRepository.findAll();
    }

    public List<Interview> getInterviewsByUserId(Long userId) {
        return interviewRepository.findAll().stream()
                .filter(i -> i.getUser().getId().equals(userId))
                .toList();
    }

    public void createInterview(InterviewRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ApiException("User not found"));

        JobApplication jobApplication = jobApplicationRepository.findById(request.getJobApplicationId())
                .orElseThrow(() -> new ApiException("Job application not found"));

        Interview interview = new Interview();
        interview.setInterviewDate(request.getInterviewDate());
        interview.setStatus(request.getStatus());
        interview.setUser(user);
        interview.setJobApplication(jobApplication);
        interview.setReminderSent(false);

        interviewRepository.save(interview);
    }

    public void updateInterview(Long id, InterviewRequest request) {
        Interview interview = interviewRepository.findById(id)
                .orElseThrow(() -> new ApiException("Interview not found"));

        interview.setInterviewDate(request.getInterviewDate());
        interview.setStatus(request.getStatus());

        interviewRepository.save(interview);
    }

    public void deleteInterview(Long id) {
        Interview interview = interviewRepository.findById(id)
                .orElseThrow(() -> new ApiException("Interview not found"));
        interviewRepository.delete(interview);
    }
}