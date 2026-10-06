package org.fadhel.tumoohplatform.dto.in;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProfileRequest {

    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name must not exceed 100 characters")
    private String fullName;

    private String profileImage;

    @Size(max = 500, message = "Bio must not exceed 500 characters")
    private String bio;

    @Pattern(regexp = "^\\+9665[0-9]{8}$", message = "Phone number must follow E.164 format (+9665XXXXXXXX)")
    private String phoneNumber;

    @NotBlank(message = "Major is required")
    private String major;

    @NotNull(message = "Graduation year is required")
    @Min(value = 1950, message = "Graduation year must be 1950 or later")
    @Max(value = 2100, message = "Graduation year cannot exceed 2100")
    private Integer graduationYear;

    @Size(max = 255, message = "skills must not exceed 255 characters")
    private String skills;

    @Size(max = 255, message = "LinkedIn URL must not exceed 255 characters")
    private String linkedinUrl;

    @Size(max = 255, message = "GitHub URL must not exceed 255 characters")
    private String githubUrl;

    @Size(max = 255, message = "CV URL must not exceed 255 characters")
    private String cvUrl;
}
