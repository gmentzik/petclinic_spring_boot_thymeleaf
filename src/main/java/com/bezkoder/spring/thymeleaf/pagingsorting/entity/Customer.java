package com.bezkoder.spring.thymeleaf.pagingsorting.entity;

import java.util.List;

import javax.persistence.*;

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

  @Column(nullable = false)
  private int level;

  @Column
  private boolean published;

  @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<Pet> pets;

  public Customer() {

  }

  public Customer(String firstName, String surName, int level, boolean published) {
    this.firstName = firstName;
    this.surName = surName;
    this.level = level;
    this.published = published;
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

  public int getLevel() {
    return level;
  }

  public void setLevel(int level) {
    this.level = level;
  }

  public boolean isPublished() {
    return published;
  }

  public void setPublished(boolean published) {
    this.published = published;
  }

  public List<Pet> getPets() {
    return pets;
  }

  public void setPets(List<Pet> pets) {
    this.pets = pets;
  }


  @Override
  public String toString() {
    return "Customer [id=" + id + ", firstName=" + firstName + ", surName=" + surName + ", level=" + level
        + ", published=" + published + "]";
  }

}
