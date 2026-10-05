package org.fadhel.tumoohplatform.dto.out;

import lombok.*;
import java.time.LocalDateTime;

@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String email;
    private LocalDateTime createdAt;
}