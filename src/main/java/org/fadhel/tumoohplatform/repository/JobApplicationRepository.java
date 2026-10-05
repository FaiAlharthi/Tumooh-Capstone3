package org.fadhel.tumoohplatform.repository;

import org.fadhel.tumoohplatform.model.Company;
import org.fadhel.tumoohplatform.model.Job;
import org.fadhel.tumoohplatform.model.JobApplication;
import org.fadhel.tumoohplatform.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

    JobApplication findJobApplicationById(Long id);
    List<JobApplication> findJobApplicationsByUser(User user);
    List<JobApplication> findJobApplicationsByUserAndStatus(User user, String status);
    boolean existsByUserAndJob(User user, Job job);
}