package org.fadhel.tumoohplatform.dto.out;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MockInterviewResultsResponse {
    private Long id;
    private String jobTitle;
    private LocalDateTime createdAt;
    private Double aiScore;
    private Double speechClarity;
    private String strengths;
    private String weaknesses;
    private String bodyLanguageTips;
    private boolean evaluationAvailable;
    private String message;
}
