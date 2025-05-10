package com.bezkoder.spring.thymeleaf.pagingsorting.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.bezkoder.spring.thymeleaf.pagingsorting.entity.Pet;
import com.bezkoder.spring.thymeleaf.pagingsorting.repository.PetRepository;

import java.util.List;

@Service
public class PetService {

  @Autowired
  private PetRepository petRepository;

  public List<Pet> getAllPets() {
    return petRepository.findAll();
  }

  public Pet getPetById(Integer id) {
    return petRepository.findById(id).orElse(null);
  }

  public Pet savePet(Pet pet) {
    return petRepository.save(pet);
  }

  public void deletePet(Integer id) {
    petRepository.deleteById(id);
  }
}