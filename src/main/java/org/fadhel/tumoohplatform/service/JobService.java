package org.fadhel.tumoohplatform.service;

import lombok.RequiredArgsConstructor;
import org.fadhel.tumoohplatform.Api.ApiException;
import org.fadhel.tumoohplatform.model.Company;
import org.fadhel.tumoohplatform.model.Job;
import org.fadhel.tumoohplatform.repository.CompanyRepository;
import org.fadhel.tumoohplatform.repository.JobRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final CompanyRepository companyRepository;

    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }

    public void addJob(Long companyId, Job job) {
        Company company = companyRepository.findCompanyById(companyId);
        if (company == null) {
            throw new ApiException("Company not found");
        }
        job.setCompany(company);
        jobRepository.save(job);
    }

    public void updateJob(Long id, Job job) {
        Job old = jobRepository.findJobById(id);
        if (old == null) {
            throw new ApiException("Job not found");
        }
        old.setPosition(job.getPosition());
        old.setDescription(job.getDescription());
        jobRepository.save(old);
    }

    public void deleteJob(Long id) {
        Job job = jobRepository.findJobById(id);
        if (job == null) {
            throw new ApiException("Job not found");
        }
        jobRepository.delete(job);
    }
}