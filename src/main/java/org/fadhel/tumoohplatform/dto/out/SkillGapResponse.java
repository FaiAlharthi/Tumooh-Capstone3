package org.fadhel.tumoohplatform.dto.out;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillGapResponse {

    private String targetRole;
    private Integer readinessScore; // Score out of 100
    private List<String> currentStrengths;
    private List<SkillGap> gaps;
    private List<LearningItem> learningPlan;
    private String overallAssessment;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SkillGap {
        private String skill;
        private String priority; // HIGH, MEDIUM, LOW
        private String reason;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LearningItem {
        private String skill;
        private String suggestion;
    }
}
