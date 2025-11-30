package com.gmentzik.spring.thymeleaf.petclinic.entity;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gmentzik.spring.thymeleaf.petclinic.dto.MedicalImageDto;

@Entity
@Table(name = "medical_history")
public class MedicalHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
  

    @ManyToOne
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    @Lob
    @Column(name = "report", columnDefinition = "TEXT")
    private String report;

    @Lob
    @Column(name = "images_json", columnDefinition = "TEXT")
    private String imagesJson;

    @Column(updatable=false)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	LocalDate created;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	LocalDate updated;

	// OnCreate, OnUpdate
	@PrePersist
	public void onCreate() {  
		 this.created = LocalDate.now(ZoneOffset.UTC);
	}
	
	@PreUpdate
	public void onUpdate() {   
		 this.updated = LocalDate.now(ZoneOffset.UTC);
	}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Pet getPet() {
        return pet;
    }
    
    // For backward compatibility with existing code
    public Pet gePet() {
        return getPet();
    }

    public void setPet(Pet pet) {
        this.pet = pet;
    }

    public String getReport() {
        return report;
    }

    public void setReport(String report) {
        this.report = report;
    }

    public LocalDate getCreated() {
        return created;
    }

    public void setCreated(LocalDate created) {
        this.created = created;
    }

    public LocalDate getUpdated() {
        return updated;
    }

    public void setUpdated(LocalDate updated) {
        this.updated = updated;
    }

    public String getImagesJson() {
        return imagesJson;
    }

    public void setImagesJson(String imagesJson) {
        this.imagesJson = imagesJson;
    }

    @Transient
    public List<MedicalImageDto> getImages() {
        if (this.imagesJson == null || this.imagesJson.trim().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(this.imagesJson, new TypeReference<List<MedicalImageDto>>() {});
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Transient
    public void setImages(List<MedicalImageDto> images) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            this.imagesJson = mapper.writeValueAsString(images);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            this.imagesJson = "[]";
        }
    }

    @Transient
    public void addImage(MedicalImageDto image) {
        List<MedicalImageDto> images = getImages();
        images.add(image);
        setImages(images);
    }

    @Override
    public String toString() {
        return "PetHistory [id=" + id + ", pet=" + pet + ", report=" + report + ", created=" + created + ", updated="
                + updated + "]";
    }
     
}