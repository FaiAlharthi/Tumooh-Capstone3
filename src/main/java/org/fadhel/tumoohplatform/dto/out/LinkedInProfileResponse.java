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
public class LinkedInProfileResponse {

    private String candidateName;
    private List<String> headlines;
    private String aboutSection;
    private List<String> keywords;
    private String tipOnUsage;
}
