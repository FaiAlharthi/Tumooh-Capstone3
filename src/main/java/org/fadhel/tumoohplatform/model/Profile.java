package org.fadhel.tumoohplatform.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Profile {

    @Id
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = true)
    private String profileImage;

    @Column(nullable = true)
    private String bio;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private String major;

    @Column(nullable = false)
    private Integer graduationYear;

    @Column(nullable = true)
    private String skills;

    @Column(nullable = true)
    private String linkedinUrl;

    @Column(nullable = true)
    private String githubUrl;

    @Column(nullable = true)
    private String cvUrl;

    @OneToOne
    @MapsId
    @JsonIgnore
    private User user;
}
