package com.gmentzik.spring.thymeleaf.petclinic.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gmentzik.spring.thymeleaf.petclinic.entity.MedicalAttachment;

@Repository
public interface MedicalAttachmentRepository extends JpaRepository<MedicalAttachment, Long> {
    List<MedicalAttachment> findByMedicalHistoryId(Integer medicalHistoryId);
    void deleteByMedicalHistoryId(Integer medicalHistoryId);
}
