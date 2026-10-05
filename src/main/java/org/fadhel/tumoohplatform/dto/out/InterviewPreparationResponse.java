package org.fadhel.tumoohplatform.dto.out;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewPreparationResponse {

    private String jobTitle;
    private String targetCompany;
    private List<String> keyPreparationTips;
    private List<String> commonInterviewQuestions;
    private List<String> recommendedSkillsToHighlight;
    private String overallStrategy;
}
