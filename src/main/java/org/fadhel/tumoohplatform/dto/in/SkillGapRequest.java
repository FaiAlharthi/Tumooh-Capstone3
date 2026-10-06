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
public class SkillGapRequest {

    @NotBlank(message = "Target role is required")
    @Size(max = 100, message = "Target role must not exceed 100 characters")
    private String targetRole;

    @Size(max = 3000, message = "Candidate summary must not exceed 3000 characters")
    private String candidateSummary;

    @Size(max = 5000, message = "Job description must not exceed 5000 characters")
    private String jobDescription;

    @Size(max = 2000, message = "Highlighted skills must not exceed 2000 characters")
    private String highlightedSkills;
}
