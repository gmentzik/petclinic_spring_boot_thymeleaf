package com.gmentzik.spring.thymeleaf.petclinic.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class FileStorageProperties {
    private String uploadDir = "./pet_images"; // Default value

    public String getUploadDir() {
        return uploadDir;
    }

    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }
}
