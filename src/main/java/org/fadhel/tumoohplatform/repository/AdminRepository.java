package org.fadhel.tumoohplatform.repository;


import org.fadhel.tumoohplatform.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRepository extends JpaRepository<Admin,Long> {
    Admin findAdminById(Long id);
}
