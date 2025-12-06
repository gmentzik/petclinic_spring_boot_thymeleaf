package com.gmentzik.spring.thymeleaf.petclinic.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gmentzik.spring.thymeleaf.petclinic.entity.MedicalHistory;
import com.gmentzik.spring.thymeleaf.petclinic.repository.MedicalHistoryRepository;
import com.gmentzik.spring.thymeleaf.petclinic.service.MedicalAttachmentService;

@Service
public class MedicalHistoryService {

    @Autowired
    private MedicalHistoryRepository medicalHistoryRepository;
    
    @Autowired
    private MedicalAttachmentService attachmentService;

    public MedicalHistory getMedicalHistoryById(Integer id) {
        return medicalHistoryRepository.findById(id).orElse(null);
    }

    @Transactional
    public MedicalHistory saveMedicalHistory(MedicalHistory medicalHistory) {
        // Save the medical history (cascades to save attachments)
        return medicalHistoryRepository.save(medicalHistory);
    }

    @Transactional
    public void deleteMedicalHistory(Integer id) {
        // Delete all attachments first
        attachmentService.deleteAttachmentsByMedicalHistoryId(id);
        // Then delete the medical history
        medicalHistoryRepository.deleteById(id);
    }
}