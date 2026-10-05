package org.fadhel.tumoohplatform.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Reminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String reminderLetter;

    @Column(nullable = false)
    private LocalDateTime reminderDate;

    @Column(nullable = false)
    private Boolean isSent;

    @ManyToOne
    @JoinColumn
    @JsonIgnore
    private JobApplication jobApplication;
}