package org.fadhel.tumoohplatform.dto.out;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MockInterviewEvaluationResponse {
    private Double aiScore;
    private Double speechClarity;
    private String strengths;
    private String weaknesses;
    private String bodyLanguageTips;
}
