package com.gmentzik.spring.thymeleaf.petclinic.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gmentzik.spring.thymeleaf.petclinic.entity.MedicalAttachment;
import com.gmentzik.spring.thymeleaf.petclinic.repository.MedicalAttachmentRepository;

@Service
public class MedicalAttachmentService {

    @Autowired
    private MedicalAttachmentRepository attachmentRepository;

    public List<MedicalAttachment> getAttachmentsByMedicalHistoryId(Integer medicalHistoryId) {
        return attachmentRepository.findByMedicalHistoryId(medicalHistoryId);
    }

    public MedicalAttachment saveAttachment(MedicalAttachment attachment) {
        return attachmentRepository.save(attachment);
    }

    public Optional<MedicalAttachment> getAttachmentById(Long id) {
        return attachmentRepository.findById(id);
    }

    public void deleteAttachment(MedicalAttachment attachment) {
        attachmentRepository.delete(attachment);
    }

    @Transactional
    public void deleteAttachment(Long attachmentId) {
        attachmentRepository.deleteById(attachmentId);
    }

    @Transactional
    public void deleteAttachmentsByMedicalHistoryId(Integer medicalHistoryId) {
        attachmentRepository.deleteByMedicalHistoryId(medicalHistoryId);
    }
}
