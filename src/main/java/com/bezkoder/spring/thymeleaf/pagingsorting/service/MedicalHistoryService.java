package com.bezkoder.spring.thymeleaf.pagingsorting.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.bezkoder.spring.thymeleaf.pagingsorting.entity.MedicalHistory;
import com.bezkoder.spring.thymeleaf.pagingsorting.repository.MedicalHistoryRepository;


@Service
public class MedicalHistoryService {

  @Autowired
  private MedicalHistoryRepository medicalHistoryRepository;

  public MedicalHistory getMedicalHistoryById(Integer id) {
    return medicalHistoryRepository.findById(id).orElse(null);
  }

  public MedicalHistory saveMeddicalHistory(MedicalHistory medicalHistory) {
    return medicalHistoryRepository.save(medicalHistory);
  }

  public void deleteMedicalHistory(Integer id) {
    medicalHistoryRepository.deleteById(id);
  }
}