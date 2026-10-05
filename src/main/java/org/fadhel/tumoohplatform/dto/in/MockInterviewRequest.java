package org.fadhel.tumoohplatform.dto.in;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor
public class MockInterviewRequest {
    
    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Job title is required")
    private String jobTitle;
}