package org.fadhel.tumoohplatform.service;

import lombok.RequiredArgsConstructor;
import org.fadhel.tumoohplatform.Api.ApiException;
import org.fadhel.tumoohplatform.model.Company;
import org.fadhel.tumoohplatform.repository.CompanyRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;

    public List<Company> getAllCompanies() {
        return companyRepository.findAll();
    }

    public void addCompany(Company company) {
        if (company.getCompanyLogoUrl() == null || company.getCompanyLogoUrl().isEmpty()) {
            company.setCompanyLogoUrl("/images/logo-placeholder.png");
        }
        companyRepository.save(company);
    }

    public void updateCompany(Long id, Company company) {
        Company old = companyRepository.findCompanyById(id);
        if (old == null) {
            throw new ApiException("Company not found");
        }
        old.setNameEn(company.getNameEn());
        old.setNameAr(company.getNameAr());
        old.setAliases(company.getAliases());
        old.setIndustryEn(company.getIndustryEn());
        old.setIndustryAr(company.getIndustryAr());
        old.setWebsite(company.getWebsite());
        old.setCompanyLogoUrl(company.getCompanyLogoUrl());
        old.setDescriptionEn(company.getDescriptionEn());
        old.setDescriptionAr(company.getDescriptionAr());
        companyRepository.save(old);
    }

    public void deleteCompany(Long id) {
        Company company = companyRepository.findCompanyById(id);
        if (company == null) {
            throw new ApiException("Company not found");
        }
        companyRepository.delete(company);
    }
}