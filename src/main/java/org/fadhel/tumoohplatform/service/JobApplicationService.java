package org.fadhel.tumoohplatform.service;

import org.fadhel.tumoohplatform.dto.in.ApplicationStatusRequest;
import org.fadhel.tumoohplatform.dto.in.DatesRequest;
import org.fadhel.tumoohplatform.dto.in.ManualApplicationRequest;
import org.fadhel.tumoohplatform.dto.out.NextStepResponse;
import org.fadhel.tumoohplatform.model.*;
import org.fadhel.tumoohplatform.repository.CompanyRepository;
import org.fadhel.tumoohplatform.repository.JobApplicationRepository;
import org.fadhel.tumoohplatform.repository.JobRepository;
import org.fadhel.tumoohplatform.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.fadhel.tumoohplatform.Api.ApiException;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


@Service
@RequiredArgsConstructor
public class JobApplicationService {

    private final JobApplicationRepository jobApplicationRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;


    private final GeminiService geminiService;
    private final ObjectMapper objectMapper;

    public List<JobApplication> getAllJobApplications() {
        return jobApplicationRepository.findAll();
    }

    public void addJobApplication(Long userId, Long jobId, JobApplication jobApplication) {
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ApiException("User not found");
        }
        Job job = jobRepository.findJobById(jobId);
        if (job == null) {
            throw new ApiException("Job not found");
        }
        jobApplication.setUser(user);
        jobApplication.setJob(job);
        if (jobApplication.getStatus() == null ) {
            jobApplication.setStatus("Applied");
        }
        jobApplication.setCreatedAt(LocalDate.now());
        jobApplicationRepository.save(jobApplication);
    }

    public void updateJobApplication(Long id, JobApplication jobApplication) {
        JobApplication old = jobApplicationRepository.findJobApplicationById(id);
        if (old == null) {
            throw new ApiException("Job application not found");
        }
        old.setStatus(jobApplication.getStatus());
        old.setClosedAt(jobApplication.getClosedAt());
        jobApplicationRepository.save(old);
    }


    public void deleteJobApplication(Long userId, Long applicationId) {
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ApiException("User not found");
        }
        JobApplication application = jobApplicationRepository.findJobApplicationById(applicationId);
        if (application == null) {
            throw new ApiException("Job application not found");
        }
        if (!application.getUser().getId().equals(userId)) {
            throw new ApiException("This application does not belong to you");
        }
        jobApplicationRepository.delete(application);
    }

    //End of CRUD endpoints


    public List<JobApplication> getMyApplications(Long userId) {
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ApiException("User not found");
        }
        return jobApplicationRepository.findJobApplicationsByUser(user);
    }


    //in case of adding the application was manually done by the seeker
    public void addManualApplication(Long userId, ManualApplicationRequest application) {
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ApiException("User not found");
        }
        //if company exist or create it with the details the user entered
        String companyName = application.getCompanyName().trim();
        Company found = companyRepository.findByAnyName(companyName);
        Company company;
        if (found != null) {
            company = found;
        } else {
            company = new Company();
            company.setNameEn(companyName);
            company.setIndustryEn("Other");
            company.setCompanyLogoUrl("/images/logo-placeholder.png");
            company.setVerified(false);
            companyRepository.save(company);
        }
        //look for same job (same company,position, and description) or create it if not found.
        String position = application.getPosition().trim();
        String description = (application.getDescription() == null) ? "Added manually by the user" : application.getDescription().trim();
        Job job = jobRepository.findJobByCompanyAndPositionIgnoreCaseAndDescriptionIgnoreCase(company, position, description);
        if (job == null) {
            job = new Job();
            job.setCompany(company);
            job.setPosition(position);
            job.setDescription(description);
            jobRepository.save(job);
        }

        //no duplicates in applications, unless the old application was rejected or withdrawn
        if (jobApplicationRepository.existsByUserAndJobAndStatusNotIn(user, job, List.of("Rejected", "Withdrawn"))) {
            throw new ApiException("You already have an active application for this job");
        }
        JobApplication jobApplication = new JobApplication();
        jobApplication.setUser(user);
        jobApplication.setJob(job);
        jobApplication.setStatus((application.getStatus() == null) ? "Applied" : application.getStatus());
        jobApplication.setCreatedAt(LocalDate.now());
        jobApplicationRepository.save(jobApplication);
    }

    //in case of updating the status of the application was manually done by the seeker
    public void updateApplicationStatus(Long userId, Long applicationId, ApplicationStatusRequest statusDto) {
        JobApplication jobApplication = jobApplicationRepository.findJobApplicationById(applicationId);
        if (jobApplication == null) {
            throw new ApiException("Job application not found");
        }
        if (!jobApplication.getUser().getId().equals(userId)) {
            throw new ApiException("This application does not belong to user with this id");
        }
        jobApplication.setStatus(statusDto.getStatus());
        jobApplicationRepository.save(jobApplication);
    }

    //get all user applications by status
    public List<JobApplication> getApplicationsByStatus(Long userId, ApplicationStatusRequest status) {
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ApiException("User not found");
        }
        List<JobApplication> applications = jobApplicationRepository.findJobApplicationsByUserAndStatus(user, status.getStatus());
        if (applications.isEmpty()) {
            throw new ApiException("No applications with status: " + status.getStatus());
        }
        return applications;
    }


    //get user history with a certain company
    public List<JobApplication> applicationsHistoryByCompany(Long userId, String companyName) {
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ApiException("User not found");
        }
        Company company = companyRepository.findByAnyName(companyName.trim());
        if (company == null) {
            throw new ApiException("Company not found");
        }
        List<JobApplication> applications = jobApplicationRepository.findJobApplicationsByUserAndJob_Company(user, company);
        if (applications.isEmpty()) {
            throw new ApiException("You have no applications with this company");
        }
        return applications;
    }

    //get all interviews done by the user for specific job application
    public Set<Interview> allApplicationInterviews(Long userId, Long applicationId) {
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ApiException("User not found");
        }
        JobApplication application = jobApplicationRepository.findJobApplicationById(applicationId);
        if (application == null) {
            throw new ApiException("Job application not found");
        }
        if (!application.getUser().getId().equals(userId)) {
            throw new ApiException("This application does not belong to user with this id");
        }

        Set<Interview> interviews = application.getInterviews();
        if (interviews.isEmpty()) {
            throw new ApiException("No interviews for this application");
        }
        return interviews;
    }

    //get all jobApplications within specific date
    public List<JobApplication> periodApplications(Long userId, DatesRequest period) {
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ApiException("User not found");
        }
        if (period.getStartDate().isAfter(period.getEndDate())) {
            throw new ApiException("Start date must be before or same as end date");
        }
        List<JobApplication> applications = jobApplicationRepository.findJobApplicationsByUserAndCreatedAtBetween(user, period.getStartDate(), period.getEndDate());
        if (applications.isEmpty()) {
            throw new ApiException("No applications in this period");
        }
        return applications;
    }
}