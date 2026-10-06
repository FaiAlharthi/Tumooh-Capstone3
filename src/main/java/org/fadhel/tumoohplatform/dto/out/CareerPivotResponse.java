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
public class CareerPivotResponse {

    private String targetRole;
    private Integer feasibilityScore; // Score out of 100
    private List<String> transferableSkills;
    private List<String> gaps;
    private List<PivotPath> pivotPaths;
    private List<String> risks;
    private String overallVerdict;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PivotPath {
        private String path;
        private String difficulty; // LOW, MEDIUM, HIGH
        private String estimatedTimeline;
        private List<String> steps;
    }
}