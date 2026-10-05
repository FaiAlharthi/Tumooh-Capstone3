package org.fadhel.tumoohplatform.repository;

import org.fadhel.tumoohplatform.model.Company;
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
            where lower(c.nameEn) like lower(concat('%', ?1, '%'))
               or c.nameAr like concat('%', ?1, '%')
               or lower(a) like lower(concat('%', ?1, '%'))
            """)
    List<Company> search(String q);

}
