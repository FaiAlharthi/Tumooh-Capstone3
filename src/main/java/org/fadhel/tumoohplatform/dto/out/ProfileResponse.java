package org.fadhel.tumoohplatform.dto.out;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProfileResponse {

    private Long userId;
    private String fullName;
    private String profileImage;
    private String bio;
    private String phoneNumber;
    private String major;
    private Integer graduationYear;
    private Set<String> skills;
    private String linkedinUrl;
    private String githubUrl;
    private String cvUrl;

}
