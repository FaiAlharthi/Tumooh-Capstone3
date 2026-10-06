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
public class CompanyBriefResponse {

    private String companyName;
    private String industry;
    private String role;
    private List<String> cultureHighlights;
    private String workEnvironment;
    private List<String> interviewInsights;
    private List<String> commonPerks;
    private List<String> researchTips;
    private String dataSourceNote;
}