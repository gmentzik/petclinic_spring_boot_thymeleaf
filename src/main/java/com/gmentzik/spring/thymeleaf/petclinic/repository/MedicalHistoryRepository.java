package com.gmentzik.spring.thymeleaf.petclinic.repository;

import jakarta.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gmentzik.spring.thymeleaf.petclinic.entity.Pet;
import com.gmentzik.spring.thymeleaf.petclinic.entity.MedicalHistory;


@Repository
@Transactional
public interface MedicalHistoryRepository extends JpaRepository<MedicalHistory, Integer> {

  // You can define custom query methods here if needed
  Page<MedicalHistory> findByPet(Pet pet, Pageable pageable);


}