package org.fadhel.tumoohplatform.dto.out;

import lombok.*;
import java.time.LocalDateTime;

@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor
public class InterviewResponse {
    private Long id;
    private Long jobApplicationId;
    private Long userId;
    private LocalDateTime interviewDate;
    private Boolean reminderSent;
    private String status;
}