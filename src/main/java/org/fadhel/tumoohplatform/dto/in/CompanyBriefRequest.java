package org.fadhel.tumoohplatform.dto.in;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyBriefRequest {

    @NotNull(message = "Company id is required")
    private Long companyId;

    @Size(max = 100, message = "Role must not exceed 100 characters")
    private String role;
}