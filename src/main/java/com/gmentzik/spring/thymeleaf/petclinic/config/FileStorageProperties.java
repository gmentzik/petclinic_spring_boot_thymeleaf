package com.gmentzik.spring.thymeleaf.petclinic.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class FileStorageProperties {
    private String uploadDir = "./pet_images"; // Default value
    
    // ID image dimensions
    private int petIdImageWidth = 200; // Default width for pet ID images
    private int petIdImageHeight = 200; // Default height for pet ID images
    
    // Medical history image dimensions
    private int petMhImageWidth = 1600; // Default width for medical history images
    private int petMhImageHeight = 1600; // Default height for medical history images

    public String getUploadDir() {
        return uploadDir;
    }

    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }

    public int getPetIdImageWidth() {
        return petIdImageWidth;
    }

    public void setPetIdImageWidth(int petIdImageWidth) {
        this.petIdImageWidth = petIdImageWidth;
    }

    public int getPetIdImageHeight() {
        return petIdImageHeight;
    }

    public void setPetIdImageHeight(int petIdImageHeight) {
        this.petIdImageHeight = petIdImageHeight;
    }

    public int getPetMhImageWidth() {
        return petMhImageWidth;
    }

    public void setPetMhImageWidth(int petMhImageWidth) {
        this.petMhImageWidth = petMhImageWidth;
    }

    public int getPetMhImageHeight() {
        return petMhImageHeight;
    }

    public void setPetMhImageHeight(int petMhImageHeight) {
        this.petMhImageHeight = petMhImageHeight;
    }
}
