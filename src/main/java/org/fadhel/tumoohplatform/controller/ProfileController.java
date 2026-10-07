package org.fadhel.tumoohplatform.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.fadhel.tumoohplatform.Api.ApiResponse;
import org.fadhel.tumoohplatform.dto.in.ProfileRequest;
import org.fadhel.tumoohplatform.dto.out.ProfileResponse;
import org.fadhel.tumoohplatform.service.ProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @PostMapping("/user/{userId}")
    public ResponseEntity<ProfileResponse> createProfile(@PathVariable Long userId, @Valid @RequestBody ProfileRequest requestDto) {
        ProfileResponse createdProfile = profileService.createProfile(userId, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProfile);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ProfileResponse> getProfileByUserId(@PathVariable Long userId) {
        ProfileResponse profile = profileService.getProfileByUserId(userId);
        return ResponseEntity.ok(profile);
    }

    @GetMapping
    public ResponseEntity<List<ProfileResponse>> getAllProfiles() {
        List<ProfileResponse> profiles = profileService.getAllProfiles();
        return ResponseEntity.ok(profiles);
    }

    @PutMapping("/user/{userId}")
    public ResponseEntity<ProfileResponse> updateProfile(
            @PathVariable Long userId,
            @Valid @RequestBody ProfileRequest requestDto) {
        ProfileResponse updatedProfile = profileService.updateProfile(userId, requestDto);
        return ResponseEntity.ok(updatedProfile);
    }

    @DeleteMapping("/user/{userId}")
    public ResponseEntity<ApiResponse> deleteProfile(@PathVariable Long userId) {
        profileService.deleteProfile(userId);
        return ResponseEntity.status(200).body(new ApiResponse("Profile deleted successfully"));
    }

    @PostMapping(value = "/user/{userId}/upload-cv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProfileResponse> uploadCv(@PathVariable Long userId, @RequestParam("file") MultipartFile file) {

        ProfileResponse updatedProfile = profileService.uploadCv(userId, file);
        return ResponseEntity.ok(updatedProfile);
    }

    @PostMapping(value = "/user/{userId}/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProfileResponse> uploadImage(@PathVariable Long userId, @RequestParam("file") MultipartFile file) {
        ProfileResponse updatedProfile = profileService.uploadImage(userId, file);
        return ResponseEntity.ok(updatedProfile);
    }

}
