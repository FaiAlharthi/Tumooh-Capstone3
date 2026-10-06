package org.fadhel.tumoohplatform.dto.in;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
public class LinkedInProfileRequest {

    @NotBlank(message = "Candidate name is required")
    @Size(max = 200, message = "Candidate name must not exceed 200 characters")
    private String candidateName;

    @Size(max = 100, message = "Current title must not exceed 100 characters")
    private String currentTitle;

    @Size(max = 100, message = "Target role must not exceed 100 characters")
    private String targetRole;

    @Size(max = 2000, message = "Skills must not exceed 2000 characters")
    private String skills;

    @Min(value = 0, message = "Years of experience must not be negative")
    @Max(value = 50, message = "Years of experience must not exceed 50")
    private Integer yearsOfExperience;

    @Size(max = 50, message = "Tone must not exceed 50 characters")
    private String tone;
}
