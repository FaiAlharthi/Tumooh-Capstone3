package org.fadhel.tumoohplatform.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "mock_interviews")
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor
public class MockInterview {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String jobTitle;

    @Column(columnDefinition = "TEXT")
    private String sessionQuestions;

    @Column(columnDefinition = "TEXT")
    private String sessionTelemetry;

    private String audioFilePath;
    private Double aiScore;
    private Double speechClarity;

    @Column(columnDefinition = "TEXT")
    private String strengths;

    @Column(columnDefinition = "TEXT")
    private String weaknesses;

    @Column(columnDefinition = "TEXT")
    private String bodyLanguageTips;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}