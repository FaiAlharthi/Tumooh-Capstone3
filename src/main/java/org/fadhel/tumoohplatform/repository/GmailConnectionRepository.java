package org.fadhel.tumoohplatform.repository;

import org.fadhel.tumoohplatform.model.GmailConnection;
import org.fadhel.tumoohplatform.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface GmailConnectionRepository extends JpaRepository<GmailConnection, Long> {
    GmailConnection findGmailConnectionByUser(User user);
}