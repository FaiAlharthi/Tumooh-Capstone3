package org.fadhel.tumoohplatform.repository;


import org.fadhel.tumoohplatform.model.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, Long> {
    List<Reminder> findByJobApplicationId(Long jobApplicationId);
}
