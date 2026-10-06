package org.fadhel.tumoohplatform.repository;

import org.fadhel.tumoohplatform.model.Company;
import org.fadhel.tumoohplatform.model.JobApplication;
import org.fadhel.tumoohplatform.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    Company findCompanyById(Long id);

    Company findCompanyByWebsite(String website);

    boolean existsByWebsite(String website);

    List<Company> findCompaniesByIndustryEn(String industryEn);

    @Query("""
        select distinct c from Company c
        left join c.aliases a
        where lower(c.nameEn) = lower(?1)
           or c.nameAr = ?1
           or lower(a) = lower(?1)
        """)
    Company findByAnyName(String name);

}
