package org.fadhel.tumoohplatform.dto.out;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InterviewUpcomingResponse {
    private Long interviewId;
    private LocalDateTime interviewDate;
    private String status;
    private String jobPosition;
    private Long jobApplicationId;
}
