package com.gmentzik.spring.thymeleaf.petclinic.entity;

import java.util.List;

import javax.persistence.*;

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

  @Enumerated(EnumType.STRING)
  private Gender gender;

  public Pet() {
  }

  public enum Gender {
    MALE,
    FEMALE
  }

  public Pet(String name, Customer customer) {
    this.name = name;
    this.customer = customer;
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

  @Override
  public String toString() {
    return "Pet [id=" + id + ", name=" + name + ", customer=" + customer + ", gender=" + gender + "]";
  }
}