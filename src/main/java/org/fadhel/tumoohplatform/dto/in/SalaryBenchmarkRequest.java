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
public class SalaryBenchmarkRequest {

    @NotBlank(message = "Target role is required")
    @Size(max = 100, message = "Target role must not exceed 100 characters")
    private String targetRole;

    @Min(value = 0, message = "Years of experience must not be negative")
    @Max(value = 50, message = "Years of experience must not exceed 50")
    private Integer yearsOfExperience;

    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @Size(max = 200, message = "Industry must not exceed 200 characters")
    private String industry;
}
