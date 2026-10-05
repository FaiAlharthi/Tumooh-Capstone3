package org.fadhel.tumoohplatform.repository;

import org.fadhel.tumoohplatform.model.Interview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InterviewRepository extends JpaRepository<Interview, Long> {
    List<Interview> findByUserId(Long userId);
    List<Interview> findByJobApplicationId(Long jobApplicationId);
    List<Interview> findByStatus(String status);
}