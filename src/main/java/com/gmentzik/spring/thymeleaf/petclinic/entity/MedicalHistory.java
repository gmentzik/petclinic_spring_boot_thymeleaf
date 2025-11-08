package com.gmentzik.spring.thymeleaf.petclinic.entity;

import java.time.LocalDate;
import java.time.ZoneOffset;

import javax.persistence.*;

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

    // JSON array string; each object: {"description":"...", "fileName":"..."}
    @Lob
    @Column(name = "attachments", columnDefinition = "TEXT")
    private String attachments;

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

    public void setPet(Pet pet) {
        this.pet = pet;
    }

    public String getReport() {
        return report;
    }

    public void setReport(String report) {
        this.report = report;
    }

    public String getAttachments() {
        return attachments;
    }

    public void setAttachments(String attachments) {
        this.attachments = attachments;
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

    @Override
    public String toString() {
        return "MedicalHistory [id=" + id + ", pet=" + pet + ", report=" + report + ", attachments=" + attachments + ", created=" + created + ", updated="
                + updated + "]";
    }
     

}