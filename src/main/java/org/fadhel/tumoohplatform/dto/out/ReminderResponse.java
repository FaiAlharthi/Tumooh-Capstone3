package org.fadhel.tumoohplatform.dto.out;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReminderResponse {

    private Long id;
    private Long jobApplicationId;
    private String reminderLetter;
    private LocalDateTime reminderDate;
    private Boolean isSent;

}
