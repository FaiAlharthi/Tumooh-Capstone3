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
public class CoverLetterResponse {

    private String jobTitle;
    private String targetCompany;
    private String subjectLine;
    private String coverLetter;
    private List<String> keyPointsUsed;
    private String tone;
}
