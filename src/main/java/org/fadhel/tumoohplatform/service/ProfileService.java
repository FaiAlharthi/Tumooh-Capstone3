package org.fadhel.tumoohplatform.service;

import lombok.RequiredArgsConstructor;
import org.fadhel.tumoohplatform.Api.ApiException;
import org.fadhel.tumoohplatform.dto.in.ProfileRequest;
import org.fadhel.tumoohplatform.dto.out.ProfileResponse;
import org.fadhel.tumoohplatform.model.Profile;
import org.fadhel.tumoohplatform.model.User;
import org.fadhel.tumoohplatform.repository.ProfileRepository;
import org.fadhel.tumoohplatform.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    @Transactional
    public ProfileResponse createProfile(Long userId, ProfileRequest requestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("User not found with id: " + userId));

        if (user.getProfile() != null) {
            throw new ApiException("Profile already exists for user id: " + userId);
        }

        Profile profile = Profile.builder()
                .user(user)
                .fullName(requestDto.getFullName())
                .profileImage(requestDto.getProfileImage())
                .bio(requestDto.getBio())
                .phoneNumber(requestDto.getPhoneNumber())
                .major(requestDto.getMajor())
                .graduationYear(requestDto.getGraduationYear())
                .skills(requestDto.getSkills())
                .linkedinUrl(requestDto.getLinkedinUrl())
                .githubUrl(requestDto.getGithubUrl())
                .cvUrl(requestDto.getCvUrl())
                .build();

        Profile savedProfile = profileRepository.save(profile);
        return mapToResponseDto(savedProfile);
    }

    public ProfileResponse getProfileByUserId(Long userId) {
        Profile profile = profileRepository.findById(userId)
                .orElseThrow(() -> new ApiException("Profile not found for user id: " + userId));
        return mapToResponseDto(profile);
    }

    public List<ProfileResponse> getAllProfiles() {
        return profileRepository.findAll()
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProfileResponse updateProfile(Long userId, ProfileRequest requestDto) {
        Profile profile = profileRepository.findById(userId)
                .orElseThrow(() -> new ApiException("Profile not found for user id: " + userId));

        profile.setFullName(requestDto.getFullName());
        profile.setProfileImage(requestDto.getProfileImage());
        profile.setBio(requestDto.getBio());
        profile.setPhoneNumber(requestDto.getPhoneNumber());
        profile.setMajor(requestDto.getMajor());
        profile.setGraduationYear(requestDto.getGraduationYear());
        profile.setSkills(requestDto.getSkills());
        profile.setLinkedinUrl(requestDto.getLinkedinUrl());
        profile.setGithubUrl(requestDto.getGithubUrl());
        profile.setCvUrl(requestDto.getCvUrl());

        Profile updatedProfile = profileRepository.save(profile);
        return mapToResponseDto(updatedProfile);
    }

    public void deleteProfile(Long userId) {
        Profile profile = profileRepository.findById(userId)
                .orElseThrow(() -> new ApiException("Profile not found for user id: " + userId));

        if (profile.getUser() != null) {
            profile.getUser().setProfile(null);
        }
        profileRepository.delete(profile);
    }

    @Transactional
    public ProfileResponse uploadCv(Long userId, MultipartFile file) {
        Profile profile = profileRepository.findById(userId)
                .orElseThrow(() -> new ApiException("Profile not found for user id: " + userId));

        // Save file and get generated URL path
        String fileUrl = fileStorageService.saveCvFile(file);

        // Store URL path in entity
        profile.setCvUrl(fileUrl);

        Profile updatedProfile = profileRepository.save(profile);
        return mapToResponseDto(updatedProfile);
    }

    private ProfileResponse mapToResponseDto(Profile profile) {
        return ProfileResponse.builder()
                .userId(profile.getUser() != null ? profile.getUser().getId() : profile.getId())
                .fullName(profile.getFullName())
                .profileImage(profile.getProfileImage())
                .bio(profile.getBio())
                .phoneNumber(profile.getPhoneNumber())
                .major(profile.getMajor())
                .graduationYear(profile.getGraduationYear())
                .skills(profile.getSkills())
                .linkedinUrl(profile.getLinkedinUrl())
                .githubUrl(profile.getGithubUrl())
                .cvUrl(profile.getCvUrl())
                .build();
    }
}
