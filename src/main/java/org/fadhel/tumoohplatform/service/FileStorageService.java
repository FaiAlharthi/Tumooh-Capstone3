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
}
