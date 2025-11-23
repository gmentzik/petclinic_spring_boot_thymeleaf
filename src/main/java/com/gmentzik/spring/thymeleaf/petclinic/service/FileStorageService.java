package com.gmentzik.spring.thymeleaf.petclinic.service;

import com.gmentzik.spring.thymeleaf.petclinic.config.FileStorageProperties;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import com.gmentzik.spring.thymeleaf.petclinic.utils.ImageType;

@Service
public class FileStorageService {
    
    private final Path fileStorageLocation;
    private final FileStorageProperties fileStorageProperties;

    public FileStorageService(FileStorageProperties fileStorageProperties) {
        this.fileStorageProperties = fileStorageProperties;
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
     * Stores a file in the configured upload directory with a unique name based on petId and image type.
     * 
     * @param file the multipart file to store (must not be null)
     * @param petId the ID of the pet associated with the file (used for naming)
     * @param imageType the type of the image (ID or MEDICAL_HISTORY)
     * @return the generated filename
     * @throws RuntimeException if the file cannot be stored or contains invalid characters
     * @throws IllegalArgumentException if the input file is null
     */
    public String storeFile(@NonNull MultipartFile file, @NonNull Integer petId, ImageType imageType) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }
        
        // Validate file name
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.trim().isEmpty()) {
            throw new IllegalArgumentException("File name cannot be null or empty");
        }
        String originalFileName = StringUtils.cleanPath(originalName);
        
        // Check for path traversal attempts
        if (originalFileName.contains("..")) {
            throw new IllegalArgumentException("Sorry! Filename contains invalid path sequence " + originalFileName);
        }
        
        // Get file extension from original filename
        String fileExtension = "";
        int lastDotIndex = originalFileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            fileExtension = originalFileName.substring(lastDotIndex).toLowerCase();
        }
        
        // Generate unique filename with appropriate extension
        boolean isImage = fileExtension.matches("\\.(jpg|jpeg|png|gif|bmp|webp)$");
        String fileName = petId + "_" + UUID.randomUUID().toString() + (isImage ? ".jpg" : fileExtension);
        
        try {
            // Read the file content
            byte[] fileContent = file.getBytes();
            byte[] processedContent;
            
            try {
                // Resize image if it's an image file
                if (file.getContentType() != null && file.getContentType().startsWith("image/")) {
                    processedContent = resizeImage(fileContent, imageType);
                } else {
                    processedContent = fileContent;
                }
            } catch (Exception e) {
                // If resizing fails, use original content with original extension
                processedContent = fileContent;
                if (isImage) {
                    // If it was supposed to be an image but resizing failed, keep original extension
                    fileName = petId + "_" + UUID.randomUUID().toString() + fileExtension;
                }
            }
            
            // Save the file
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            try (InputStream inputStream = new ByteArrayInputStream(processedContent)) {
                Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            }
            return fileName;
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }
    
    /**
     * Resizes an image to the appropriate dimensions based on image type while maintaining aspect ratio
     * and converts it to JPG format with a white background.
     * Only call this method with known image files.
     * 
     * @param imageData the image data to resize
     * @param imageType the type of the image (ID or MEDICAL_HISTORY)
     * @return the resized image as a byte array in JPG format
     * @throws IOException if the image cannot be read or written, or if the format is unsupported
     */
    private byte[] resizeImage(byte[] imageData, ImageType imageType) throws IOException {
        BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageData));
        if (originalImage == null) {
            throw new IOException("Unsupported image format");
        }

        // Get target dimensions based on image type
        int targetWidth, targetHeight;
        if (imageType == ImageType.MEDICAL_HISTORY) {
            targetWidth = fileStorageProperties.getPetMhImageWidth();
            targetHeight = fileStorageProperties.getPetMhImageHeight();
        } else if (imageType == ImageType.PET_ID) {
            // Default to ID image dimensions
            targetWidth = fileStorageProperties.getPetIdImageWidth();
            targetHeight = fileStorageProperties.getPetIdImageHeight();
        } else {
            throw new IllegalArgumentException("Unsupported image type: " + imageType);
        }
        
        // Create new image with white background
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resizedImage.createGraphics();
        
        // Fill with white background
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, targetWidth, targetHeight);
        
        // Calculate dimensions to maintain aspect ratio
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();
        
        // Calculate the new dimensions while maintaining aspect ratio
        int newWidth = originalWidth;
        int newHeight = originalHeight;
        
        // First check if we need to scale down
        if (originalWidth > targetWidth || originalHeight > targetHeight) {
            double widthRatio = (double) targetWidth / (double) originalWidth;
            double heightRatio = (double) targetHeight / (double) originalHeight;
            double ratio = Math.min(widthRatio, heightRatio);
            
            newWidth = (int) (originalWidth * ratio);
            newHeight = (int) (originalHeight * ratio);
        }
        
        // Calculate position to center the image
        int x = (targetWidth - newWidth) / 2;
        int y = (targetHeight - newHeight) / 2;
        
        // Draw the resized image
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.drawImage(originalImage, x, y, newWidth, newHeight, null);
        g.dispose();

        // Convert to byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(resizedImage, "jpg", baos);
        return baos.toByteArray();
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
