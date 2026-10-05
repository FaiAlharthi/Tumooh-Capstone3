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
public class InterviewPreparationRequest {

    @NotBlank(message = "Job title is required")
    @Size(max = 100, message = "Job title must not exceed 100 characters")
    private String jobTitle;

    @Size(max = 500, message = "Company name must not exceed 500 characters")
    private String targetCompany;

    @Size(max = 1000, message = "Job description must not exceed 1000 characters")
    private String jobDescription;
}
