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
public class MockInterviewSummaryResponse {
    private Long userId;
    private long totalSessions;
    private Double averageAiScore;
    private LocalDateTime lastSessionAt;
}
