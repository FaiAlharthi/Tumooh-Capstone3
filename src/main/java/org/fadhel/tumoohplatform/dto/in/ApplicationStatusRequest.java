package org.fadhel.tumoohplatform.dto.in;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationStatusRequest {
    @NotEmpty(message = "Status is required")
    @Pattern(regexp = "^(Applied|InProgress|Offered|Rejected|Withdrawn)$",
            message = "Status must be one of: Applied, InProgress, Offered, Rejected, Withdrawn")
    private String status;
}
