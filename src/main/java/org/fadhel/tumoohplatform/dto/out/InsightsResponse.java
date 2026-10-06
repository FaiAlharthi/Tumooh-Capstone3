package org.fadhel.tumoohplatform.dto.out;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InsightsResponse {
    private Integer totalApplications;
    private Integer appliedCount;
    private Integer inProgressCount;
    private Integer offeredCount;
    private Integer rejectedCount;
    private Integer withdrawnCount;
    private List<String> industries;
    private Integer totalInterviews;

    //from Gemini
    private List<String> strengths;
    private List<String> concerns;
    private List<String> recommendations;
}