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
public class SalaryBenchmarkResponse {

    private String targetRole;
    private String city;
    private String currency;
    private EstimatedRange estimatedRange;
    private PercentileBands percentileBands;
    private String experienceLevel;
    private List<String> factorsAffectingPay;
    private List<String> negotiationTips;
    private String confidence;
    private String disclaimer;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EstimatedRange {
        private Long min;
        private Long typical;
        private Long max;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PercentileBands {
        private Long p25;
        private Long p50;
        private Long p75;
    }
}
