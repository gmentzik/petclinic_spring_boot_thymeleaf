package com.gmentzik.spring.thymeleaf.petclinic.entity;

import java.util.List;

import javax.persistence.*;
import javax.validation.constraints.Pattern;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;

@Entity
@Table(name = "customer")
public class Customer {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Integer id;

  @Column(length = 128, nullable = false)
  private String firstName;

  @Column(length = 256)
  private String surName;

  @Column(unique = true)
  @Pattern(regexp = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}", message = "Invalid email format")
  private String email;

  @Column(length = 16)
  private String phone;

  @Column(length = 256)
  private String address;

  @Column(length = 128)
  private String city;

  @Column(length = 128)
  private String state;

  @Column(length = 16)
  private String zipCode;

  @Column(name = "entry_date", nullable = false)
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
  private LocalDate entryDate;

  public Customer(String firstName, String surName, String email, String phone, String address, 
                 String city, String state, String zipCode, LocalDate entryDate, boolean active, 
                 String note1, String note2, String note3) {
    this.firstName = firstName;
    this.surName = surName;
    this.email = email;
    this.phone = phone;
    this.address = address;
    this.city = city;
    this.state = state;
    this.zipCode = zipCode;
    this.entryDate = entryDate != null ? entryDate : LocalDate.now();
    this.active = active;
    this.note1 = note1;
    this.note2 = note2;
    this.note3 = note3;
  }

  @Column
  private boolean active = true;

  @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<Pet> pets;

  @Column(columnDefinition = "TEXT")
  private String note1;

  @Column(columnDefinition = "TEXT")
  private String note2;

  @Column(columnDefinition = "TEXT")
  private String note3;

  public Customer() {
    this.entryDate = LocalDate.now();
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getSurName() {
    return surName;
  }

  public void setSurName(String surName) {
    this.surName = surName;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
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

  public List<Pet> getPets() {
    return pets;
  }

  public void setPets(List<Pet> pets) {
    this.pets = pets;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public String getZipCode() {
    return zipCode;
  }

  public void setZipCode(String zipCode) {
    this.zipCode = zipCode;
  }

  public LocalDate getEntryDate() {
    return entryDate;
  }

  public void setEntryDate(LocalDate entryDate) {
    this.entryDate = entryDate != null ? entryDate : LocalDate.now();
  }


  @Override
  public String toString() {
    return "Customer [id=" + id + ", firstName=" + firstName + ", surName=" + surName
        + ", active=" + active + "entryDate=" + entryDate + "email=" + email + "phone=" + phone + "address=" + address + "city=" + city + "state=" + state + "zipCode=" + zipCode + "]";
  }

}
