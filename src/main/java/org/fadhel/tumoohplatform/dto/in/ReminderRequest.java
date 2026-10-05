package org.fadhel.tumoohplatform.dto.in;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReminderRequest {

    @NotNull(message = "Job application ID is required")
    private Long jobApplicationId;

    @NotBlank(message = "Reminder letter is required")
    @Size(max = 2000, message = "Reminder letter must not exceed 2000 characters")
    private String reminderLetter;

    @NotNull(message = "Reminder date is required")
    private LocalDateTime reminderDate;

    @NotNull(message = "isSent flag is required")
    private Boolean isSent;

}
