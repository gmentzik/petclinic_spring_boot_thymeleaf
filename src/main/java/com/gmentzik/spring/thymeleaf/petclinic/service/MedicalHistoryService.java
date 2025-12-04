package com.gmentzik.spring.thymeleaf.petclinic.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.gmentzik.spring.thymeleaf.petclinic.entity.MedicalHistory;
import com.gmentzik.spring.thymeleaf.petclinic.repository.MedicalHistoryRepository;


@Service
public class MedicalHistoryService {

  @Autowired
  private MedicalHistoryRepository medicalHistoryRepository;

  public MedicalHistory getMedicalHistoryById(Integer id) {
    return medicalHistoryRepository.findById(id).orElse(null);
  }

  public MedicalHistory saveMedicalHistory(MedicalHistory medicalHistory) {
    return medicalHistoryRepository.save(medicalHistory);
  }

  public void deleteMedicalHistory(Integer id) {
    medicalHistoryRepository.deleteById(id);
  }
}