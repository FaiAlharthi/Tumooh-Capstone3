package org.fadhel.tumoohplatform.service;


import lombok.RequiredArgsConstructor;
import org.fadhel.tumoohplatform.dto.in.SalaryBenchmarkRequest;
import org.fadhel.tumoohplatform.dto.out.SalaryBenchmarkResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SalaryBenchmarkService {

    private static final String DISCLAIMER =
            "AI-generated estimate for guidance only. This is not official salary data. "
                    + "Actual salaries vary by employer, contract terms and market conditions. "
                    + "Verify with current local job market sources before making decisions.";

    private final GeminiService geminiService;
    private final ObjectMapper objectMapper;

    public SalaryBenchmarkResponse benchmarkSalary(SalaryBenchmarkRequest request) {
        String prompt = String.format("""
            Act as an expert Saudi Arabian compensation and career consultant.
            Provide an annual salary benchmark (in SAR) for the role of '%s'.

            Years of experience: %s

            Location: %s

            Industry: %s

            Strictly return ONLY a valid JSON object matching this exact schema (no markdown formatting, no text outside JSON, all monetary values are annual SAR numbers):
            {
              "estimatedRange": {"min": 120000, "typical": 180000, "max": 260000},
              "percentileBands": {"p25": 140000, "p50": 180000, "p75": 230000},
              "experienceLevel": "Mid-level",
              "factorsAffectingPay": ["Factor 1 affecting pay for this role", "Factor 2"],
              "negotiationTips": ["Practical negotiation tip 1 for this role/market", "Practical negotiation tip 2"],
              "confidence": "Medium - general market estimate"
            }
            """,
                request.getTargetRole(),
                (request.getYearsOfExperience() != null
                        ? request.getYearsOfExperience() + " years"
                        : "Not specified"),
                (request.getCity() != null ? request.getCity() : "Saudi Arabia (national average)"),
                (request.getIndustry() != null ? request.getIndustry() : "General market")
        );

        String rawAiOutput = geminiService.generateText(prompt);

        try {
            String cleanJson = rawAiOutput.replaceAll("```json|```", "").trim();

            SalaryBenchmarkResponse response = objectMapper.readValue(cleanJson, SalaryBenchmarkResponse.class);
            response.setTargetRole(request.getTargetRole());
            response.setCity(request.getCity() != null ? request.getCity() : "Saudi Arabia (national average)");
            response.setCurrency("SAR");
            response.setDisclaimer(DISCLAIMER);
            return response;

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "AI generation failed. Please try again.");
        }
    }

}
