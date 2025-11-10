package com.gmentzik.spring.thymeleaf.petclinic.service;

import com.gmentzik.spring.thymeleaf.petclinic.config.FileStorageProperties;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {
    
    private final Path fileStorageLocation;

    public FileStorageService(FileStorageProperties fileStorageProperties) {
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir())
                .toAbsolutePath()
                .normalize();
        
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create upload directory: " + this.fileStorageLocation, ex);
        }
    }

    /**
     * Stores a file in the configured upload directory with a unique name based on petId.
     *
     * @param file the multipart file to store (must not be null)
     * @param petId the ID of the pet associated with the file (used for naming)
     * @return the generated filename
     * @throws RuntimeException if the file cannot be stored or contains invalid characters
     * @throws IllegalArgumentException if the input file is null
     */
    public String storeFile(@NonNull MultipartFile file, @NonNull Integer petId) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }
        
        // Generate unique filename
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.trim().isEmpty()) {
            throw new IllegalArgumentException("File name cannot be null or empty");
        }
        String originalFileName = StringUtils.cleanPath(originalName);
        
        String fileExtension = "";
        int lastDotIndex = originalFileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            fileExtension = originalFileName.substring(lastDotIndex);
        }
        
        String fileName = petId + "_" + UUID.randomUUID().toString() + fileExtension;
        
        try {
            // Check if the file's name contains invalid characters
            if (originalFileName.contains("..")) {
                throw new RuntimeException("Sorry! Filename contains invalid path sequence " + originalFileName);
            }

            // Copy file to the target location (Replacing existing file with the same name)
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return fileName;
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }

    /**
     * Loads a file from the storage location and returns it as a Spring Resource.
     *
     * @param fileName the name of the file to load (must not be null or empty)
     * @return the loaded file as a Resource object
     * @throws RuntimeException if the file is not found or cannot be read
     */
    public Resource loadFileAsResource(@NonNull String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException("File name cannot be null or empty");
        }
        
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new RuntimeException("File not found " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException("File not found " + fileName, ex);
        }
    }

    /**
     * Deletes a file from the storage location if it exists.
     *
     * @param fileName the name of the file to delete (must not be null or empty)
     * @throws RuntimeException if the file exists but cannot be deleted
     */
    public void deleteFile(@NonNull String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException("File name cannot be null or empty");
        }
        
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            throw new RuntimeException("Could not delete file " + fileName, ex);
        }
    }
}
