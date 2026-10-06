package org.fadhel.tumoohplatform.dto.in;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoverLetterRequest {

    @NotBlank(message = "Job title is required")
    @Size(max = 100, message = "Job title must not exceed 100 characters")
    private String jobTitle;

    @Size(max = 500, message = "Company name must not exceed 500 characters")
    private String targetCompany;

    @Size(max = 5000, message = "Job description must not exceed 5000 characters")
    private String jobDescription;

    @NotBlank(message = "Candidate name is required")
    @Size(max = 200, message = "Candidate name must not exceed 200 characters")
    private String candidateName;

    @Size(max = 3000, message = "Candidate summary must not exceed 3000 characters")
    private String candidateSummary;

    @Size(max = 50, message = "Tone must not exceed 50 characters")
    private String tone;
}
