package com.gmentzik.spring.thymeleaf.petclinic.entity;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import jakarta.persistence.*;

import com.fasterxml.jackson.annotation.JsonFormat;

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


    @OneToMany(mappedBy = "medicalHistory", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<MedicalAttachment> attachmentFiles = new HashSet<>();

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

    // Attachment management methods
    public Set<MedicalAttachment> getAttachmentFiles() {
        return attachmentFiles;
    }

    public void setAttachmentFiles(Set<MedicalAttachment> attachmentFiles) {
        this.attachmentFiles = attachmentFiles;
    }
    
    public void addAttachment(MedicalAttachment attachment) {
        attachmentFiles.add(attachment);
        attachment.setMedicalHistory(this);
    }
    
    public void removeAttachment(MedicalAttachment attachment) {
        attachmentFiles.remove(attachment);
        attachment.setMedicalHistory(null);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MedicalHistory that = (MedicalHistory) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "MedicalHistory{" +
                "id=" + id +
                ", pet=" + (pet != null ? pet.getId() : null) +
                ", created=" + created +
                ", updated=" + updated +
                '}';
    }
}