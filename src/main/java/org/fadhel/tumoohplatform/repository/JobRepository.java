package org.fadhel.tumoohplatform.repository;

import org.fadhel.tumoohplatform.model.Company;
import org.fadhel.tumoohplatform.model.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    Job findJobById(Long id);

    List<Job> findJobsByCompany(Company company);

    List<Job> findJobsByPositionContainingIgnoreCase(String position);

    Job findJobByCompanyAndPositionIgnoreCaseAndDescriptionIgnoreCase(Company company, String position, String description);
}