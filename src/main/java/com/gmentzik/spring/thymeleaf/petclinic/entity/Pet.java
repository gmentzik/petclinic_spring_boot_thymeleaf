package com.gmentzik.spring.thymeleaf.petclinic.entity;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.*;

import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name = "pet")
public class Pet {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Integer id;

  @Column(length = 128, nullable = false)
  private String name;

  @ManyToOne
  @JoinColumn(name = "customer_id", nullable = false)
  private Customer customer;

  @OneToMany(mappedBy = "pet", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<MedicalHistory> medicalHistory;

  @Column(name = "animal_type")
  @Enumerated(EnumType.STRING)
  private AnimalType animalType;

  @Column(length = 128)
  private String breed;

  @Enumerated(EnumType.STRING)
  private Gender gender;

  
  @Enumerated(EnumType.STRING)
  @Column(name = "neutered")
  private Neutered neutered = Neutered.UNKNOWN;

  @Column(name = "entry_date", nullable = false)
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
  private LocalDate entryDate;

  @Column(name = "birth_date")
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
  private LocalDate birthDate;

  @Column(columnDefinition = "TEXT")
  private String note1;

  @Column(columnDefinition = "TEXT")
  private String note2;

  @Column(columnDefinition = "TEXT")
  private String note3;

  @Column(name = "photo_filename", length = 255)
  private String photoFilename;

  public Pet() {
    this.entryDate = LocalDate.now();
  }

  public enum Gender {
    MALE,
    FEMALE
  }

  public enum Neutered {
    YES,
    NO,
    UNKNOWN
  }


  public enum AnimalType {
    DOG,
    CAT,
    BIRD,
    FISH,
    REPTILE,
    OTHER
  }

  public Pet(String name, Customer customer) {
    this.name = name;
    this.customer = customer;
  }

  public Pet(String name,
             Customer customer,
             AnimalType animalType,
             String breed,
             Gender gender,
             Neutered neutered,
             LocalDate entryDate,
             LocalDate birthDate,
             String note1,
             String note2,
             String note3) {
    this.name = name;
    this.customer = customer;
    this.animalType = animalType;
    this.breed = breed;
    this.gender = gender;
    this.neutered = neutered;
    this.entryDate = entryDate != null ? entryDate : LocalDate.now();
    this.birthDate = birthDate;
    this.note1 = note1;
    this.note2 = note2;
    this.note3 = note3;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Customer getCustomer() {
    return customer;
  }

  public void setCustomer(Customer customer) {
    this.customer = customer;
  }

  public List<MedicalHistory> getMedicalHistory() {
    return medicalHistory;
  }

  public void setMedicalHistory(List<MedicalHistory> medicalHistory) {
    this.medicalHistory = medicalHistory;
  }

  public Gender getGender() {
    return gender;
  }

  public void setGender(Gender gender) {
    this.gender = gender;
  }


  public Neutered getNeutered() {
    return neutered;
  }

  public void setNeutered(Neutered neutered) {
    this.neutered = neutered;
  }

  public AnimalType getAnimalType() {
    return animalType;
  }

  public void setAnimalType(AnimalType animalType) {
    this.animalType = animalType;
  }

  public String getBreed() {
    return breed;
  }

  public void setBreed(String breed) {
    this.breed = breed;
  }

  public LocalDate getEntryDate() {
    return entryDate;
  }

  public void setEntryDate(LocalDate entryDate) {
    this.entryDate = entryDate != null ? entryDate : LocalDate.now();
  }

  public LocalDate getBirthDate() {
    return birthDate;
  }

  public void setBirthDate(LocalDate birthDate) {
    this.birthDate = birthDate;
  }

  public String getNote1() {
    return note1;
  }

  public void setNote1(String note1) {
    this.note1 = note1;
  }

  public String getNote2() {
    return note2;
  }

  public void setNote2(String note2) {
    this.note2 = note2;
  }

  public String getNote3() {
    return note3;
  }

  public void setNote3(String note3) {
    this.note3 = note3;
  }

  public String getPhotoFilename() {
    return photoFilename;
  }

  public void setPhotoFilename(String photoFilename) {
    this.photoFilename = photoFilename;
  }

  @Override
  public String toString() {
    Integer customerId = customer != null ? customer.getId() : null;
    int medicalHistoryCount = medicalHistory != null ? medicalHistory.size() : 0;
    return "Pet{" +
        "id=" + id +
        ", name='" + name + '\'' +
        ", customerId=" + customerId +
        ", animalType=" + animalType +
        ", breed='" + breed + '\'' +
        ", gender=" + gender +
        ", neutered=" + neutered +
        ", entryDate=" + entryDate +
        ", birthDate=" + birthDate +
        ", note1='" + note1 + '\'' +
        ", note2='" + note2 + '\'' +
        ", note3='" + note3 + '\'' +
        ", photoFilename='" + photoFilename + '\'' +
        ", medicalHistoryCount=" + medicalHistoryCount +
        '}';
  }
}