package org.fadhel.tumoohplatform.dto.out;

import lombok.*;
import java.time.LocalDateTime;

@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor
public class MockInterviewResponse {
    private Long id;
    private Long userId;
    private String jobTitle;
    private String audioFilePath;
    private Double aiScore;
    private String strengths;
    private String weaknesses;
    private String bodyLanguageTips;
    private LocalDateTime createdAt;
}