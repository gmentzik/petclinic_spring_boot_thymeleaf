package com.gmentzik.spring.thymeleaf.petclinic.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MedicalImageDto {
    private String imageName;
    private String imageDescription;

    public MedicalImageDto() {
    }

    public MedicalImageDto(String imageName, String imageDescription) {
        this.imageName = imageName;
        setImageDescription(imageDescription);
    }

    // Getters and Setters
    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getImageDescription() {
        return imageDescription;
    }

    public void setImageDescription(String imageDescription) {
        this.imageDescription = imageDescription == null ? "" : imageDescription.trim();
    }
}
