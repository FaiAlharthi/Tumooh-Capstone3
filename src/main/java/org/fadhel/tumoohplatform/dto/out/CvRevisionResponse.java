package org.fadhel.tumoohplatform.dto.out;


import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CvRevisionResponse {

    private String fileName;
    private Integer atsScore; // Score out of 100
    private List<String> keyStrengths;
    private List<String> areasForImprovement;
    private String revisedProfessionalSummary;
    private List<String> revisedBulletPoints;
    private String formattingAndToneFeedback;
}
