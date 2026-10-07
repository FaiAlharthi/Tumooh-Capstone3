package org.fadhel.tumoohplatform.service;

import org.fadhel.tumoohplatform.Api.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private final String uploadDir = "uploads/cvs/";

    public String saveCvFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot upload empty file");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals("application/pdf")) {
            throw new ApiException("Only PDF files are allowed");
        }

        try {
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // Generate unique filename to prevent overwrites
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path targetLocation = Paths.get(uploadDir).resolve(fileName);

            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Returns relative web path (e.g., "/uploads/cvs/uuid_resume.pdf")
            return "/uploads/cvs/" + fileName;

        } catch (IOException e) {
            throw new RuntimeException("Could not store file. Please try again!", e);        }
    }

    private final String imageUploadDir = "uploads/images/";
    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif");
    private static final long MAX_IMAGE_BYTES = 5L * 1024 * 1024; // 5 MB

    public String saveImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApiException("Cannot upload an empty file");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType)) {
            throw new ApiException("Only JPG, PNG, WebP or GIF images are supported");
        }
        if (file.getSize() > MAX_IMAGE_BYTES) {
            throw new ApiException("Image must be 5 MB or smaller");
        }

        String extension = switch (contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> ".gif";
        };

        try {
            File dir = new File(imageUploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String fileName = UUID.randomUUID() + extension;
            Path targetLocation = Paths.get(imageUploadDir).resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return "/uploads/images/" + fileName;
        } catch (IOException e) {
            throw new ApiException("Could not store the image. Please try again!");
        }
    }
}
