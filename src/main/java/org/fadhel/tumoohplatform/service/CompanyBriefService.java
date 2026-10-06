package org.fadhel.tumoohplatform.service;


import lombok.RequiredArgsConstructor;
import org.fadhel.tumoohplatform.dto.in.CompanyBriefRequest;
import org.fadhel.tumoohplatform.dto.out.CompanyBriefResponse;
import org.fadhel.tumoohplatform.model.Company;
import org.fadhel.tumoohplatform.repository.CompanyRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class CompanyBriefService {

    private static final String DATA_SOURCE_NOTE =
            "AI-generated from the industry and public information available in Tumooh's database "
                    + "- not official employer data. Verify with the company website, LinkedIn and current "
                    + "employees before making decisions.";

    private final GeminiService geminiService;
    private final ObjectMapper objectMapper;
    private final CompanyRepository companyRepository;

    public CompanyBriefResponse generateCompanyBrief(CompanyBriefRequest request) {
        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found."));

        String roleContext = (request.getRole() != null ? request.getRole() : "not specified");

        String prompt = String.format("""
            Act as an expert workplace culture researcher for the Saudi Arabian job market.
            Produce a company culture brief for job seekers applying to '%s'.

            Company name (English): %s
            Company name (Arabic): %s
            Industry: %s
            Company description: %s
            Website: %s

            Role the candidate is targeting: %s

            Base every claim on the industry, description and well-known public information. Where you are
            uncertain, say so honestly instead of inventing specifics.

            Strictly return ONLY a valid JSON object matching this exact schema (no markdown formatting, no text outside JSON):
            {
              "cultureHighlights": ["Inferred work style or value 1", "Inferred work style or value 2"],
              "workEnvironment": "Description of the likely day-to-day work environment based on industry and company info.",
              "interviewInsights": ["Typical interview process or preparation pointer 1", "Pointer 2"],
              "commonPerks": ["Industry-typical perk 1", "Industry-typical perk 2"],
              "researchTips": ["How the candidate can verify this themselves: source 1", "Source 2"]
            }
            """,
                company.getNameEn(),
                company.getNameEn(),
                (company.getNameAr() != null ? company.getNameAr() : "-"),
                company.getIndustryEn(),
                (company.getDescriptionEn() != null ? company.getDescriptionEn() : "No description on file"),
                (company.getWebsite() != null ? company.getWebsite() : "Not listed"),
                roleContext
        );

        String rawAiOutput = geminiService.generateText(prompt);

        try {
            String cleanJson = rawAiOutput.replaceAll("```json|```", "").trim();

            CompanyBriefResponse response = objectMapper.readValue(cleanJson, CompanyBriefResponse.class);
            response.setCompanyName(company.getNameEn());
            response.setIndustry(company.getIndustryEn());
            response.setRole(request.getRole());
            response.setDataSourceNote(DATA_SOURCE_NOTE);
            return response;

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "AI generation failed. Please try again.");
        }
    }

}