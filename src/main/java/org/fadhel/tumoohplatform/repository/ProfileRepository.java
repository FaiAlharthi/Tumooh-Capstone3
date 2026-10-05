package org.fadhel.tumoohplatform.repository;

import org.fadhel.tumoohplatform.model.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {
    Profile findProfileById(Long id);
}
