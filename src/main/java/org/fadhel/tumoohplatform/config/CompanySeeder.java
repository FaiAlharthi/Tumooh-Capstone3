package org.fadhel.tumoohplatform.config;

import lombok.RequiredArgsConstructor;
import org.fadhel.tumoohplatform.model.Company;
import org.fadhel.tumoohplatform.repository.CompanyRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CompanySeeder implements CommandLineRunner {

    private final CompanyRepository companyRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void run(String... args) throws Exception {
        if (companyRepository.count() > 0) {
            return;
        }

        InputStream in = new ClassPathResource("data/saudi_companies.json").getInputStream();
        List<Company> companies = objectMapper.readValue(in, new TypeReference<>() {});

        companies.forEach(c -> c.setId(null));
        companyRepository.saveAll(companies);
    }
}