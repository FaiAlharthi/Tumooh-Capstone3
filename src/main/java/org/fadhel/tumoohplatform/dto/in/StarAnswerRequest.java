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
public class StarAnswerRequest {

    @NotBlank(message = "Question is required")
    @Size(max = 1000, message = "Question must not exceed 1000 characters")
    private String question;

    @NotBlank(message = "Raw story is required")
    @Size(max = 4000, message = "Raw story must not exceed 4000 characters")
    private String rawStory;

    @Size(max = 100, message = "Target role must not exceed 100 characters")
    private String targetRole;

    @Size(max = 50, message = "Tone must not exceed 50 characters")
    private String tone;
}