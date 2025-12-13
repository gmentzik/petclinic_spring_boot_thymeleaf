package com.gmentzik.spring.thymeleaf.petclinic.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.server.ResponseStatusException;

import com.gmentzik.spring.thymeleaf.petclinic.entity.Pet;
import com.gmentzik.spring.thymeleaf.petclinic.repository.MedicalHistoryRepository;
import com.gmentzik.spring.thymeleaf.petclinic.entity.MedicalHistory;
import com.gmentzik.spring.thymeleaf.petclinic.entity.Customer;
import com.gmentzik.spring.thymeleaf.petclinic.entity.MedicalAttachment;
import com.gmentzik.spring.thymeleaf.petclinic.common.enums.ImageType;
import com.gmentzik.spring.thymeleaf.petclinic.service.PetService;
import com.gmentzik.spring.thymeleaf.petclinic.service.FileStorageService;
import com.gmentzik.spring.thymeleaf.petclinic.service.MedicalHistoryService;
import com.gmentzik.spring.thymeleaf.petclinic.service.MedicalAttachmentService;

@Controller
public class MedicalHistoryController {

    @Autowired
    private PetService petsService;

    @Autowired
    private MedicalHistoryService medicalHistoryService;

    @Autowired
    private MedicalHistoryRepository medicalHistoryRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private MedicalAttachmentService attachmentService;

    @GetMapping("/customers/{cId}/pets/{petId}/medicalhistory")
    public String getAuthorMedicalHistory(
            @PathVariable("petId") Integer petId,
            @PathVariable("cId") Integer urlCustomerId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "6") int size,
            @RequestParam(defaultValue = "id,desc") String[] sort,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            System.out.println("IN GET MEDICAL LIST FOR PET:" + petId);
            // Retrieve the author based on the authorId
            Pet pet = petsService.getPetById(petId);
            Integer customerId = pet.getCustomer().getId();

            String sortField = sort[0];
            String sortDirection = sort[1];

            Direction direction = sortDirection.equals("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
            Order order = new Order(direction, sortField);

            Pageable pageable = PageRequest.of(page - 1, size, Sort.by(order));
            // Test Pageable pageable = PageRequest.of(0, 6);

            System.out.println("pageable");
            System.out.println(pageable);
            System.out.println("pageable");
            Page<MedicalHistory> medicalHistoryListPage;
            medicalHistoryListPage = medicalHistoryRepository.findByPet(pet, pageable);
            Customer customer = pet.getCustomer();
            // Add the medical history list to the model
            model.addAttribute("customer", customer);
            model.addAttribute("pet", pet);
            model.addAttribute("medicalHistoryList", medicalHistoryListPage.getContent());
            model.addAttribute("customerTitle", customer.getFirstName() + " " + customer.getSurName());
            model.addAttribute("currentPage", medicalHistoryListPage.getNumber() + 1);
            model.addAttribute("totalItems", medicalHistoryListPage.getTotalElements());
            model.addAttribute("totalPages", medicalHistoryListPage.getTotalPages());
            model.addAttribute("pageSize", size);
            model.addAttribute("sortField", sortField);
            model.addAttribute("sortDirection", sortDirection);
            model.addAttribute("reverseSortDirection", sortDirection.equals("asc") ? "desc" : "asc");

            model.addAttribute("pageTitle",
                    "Medical History for Pet ID: " + petId + ",(Customer IDs: " + customerId + " )");
            return "medical_history"; // Assuming there is a view named "medical_history_view" to display the medical
                                      // history

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", e.getMessage());
            return "redirect:/customers/" + urlCustomerId + "/pets";
        }
    }

    // New Author
    @GetMapping("/pets/{petId}/medicalhistoryrecord/new")
    public String addMedicalRecord(
            @PathVariable("petId") Integer petId,
            Model model) {
        // Create new author object
        MedicalHistory mh = new MedicalHistory();
        Pet pet = petsService.getPetById(petId);
        Customer customer = pet.getCustomer();
        mh.setPet(pet);

        model.addAttribute("customer", customer);
        model.addAttribute("pet", pet);
        model.addAttribute("medicalhistory", mh);
        model.addAttribute("pageTitle", "Create new medical record");

        return "medical_record_form";
    }

    @GetMapping("/customers/{cId}/pets/{petId}/medicalhistoryrecord/{recordId}/edit")
    public String editMedicalRecord(
            @PathVariable("cId") Integer urlCustomerId,
            @PathVariable("petId") Integer petId,
            @PathVariable("recordId") Integer recordId,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            System.out.println("IN EDIT MEDICAL RECORD");
            MedicalHistory mhr = medicalHistoryService.getMedicalHistoryById(recordId);
            Pet pet = mhr.gePet();
            Customer customer = pet.getCustomer();
            model.addAttribute("customer", customer);
            model.addAttribute("pet", pet);
            model.addAttribute("medicalhistory", mhr);
            model.addAttribute("pageTitle", "Edit medical record");

            return "medical_record_form";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", e.getMessage());
            return "redirect:/customers/" + urlCustomerId + "/pets/" + petId + "/medicalhistory";
        }
    }

    @PostMapping("/customers/{cId}/pets/{pId}/medicalhistoryrecord/save")
    @Transactional
    public String saveMedicalRecord(
            @ModelAttribute MedicalHistory mhr,
            @PathVariable("cId") Integer urlCustomerId,
            @PathVariable("pId") Integer petId,
            Model model,
            @RequestPart(name = "images", required = false) List<MultipartFile> files,
            @RequestParam(name = "descriptions", required = false) List<String> descriptions,
            RedirectAttributes redirectAttributes) {

        try {
            System.out.println("SAVE MEDICAL HISTORY REPORT");
            System.out.println(mhr);
            System.out.println("Number of attachments: " + files.size());
            System.out.println("Number of descriptions: " + descriptions.size());

            if (mhr.getId() == null) {
                System.out.println("NEW MEDICAL HISTORY REPORT");
            } else {
                System.out.println("EDIT MEDICAL RECORD");
                // Load the existing record with its attachments
                MedicalHistory existingRecord = medicalHistoryService.getMedicalHistoryById(mhr.getId());
                if (existingRecord != null) {
                    // Copy the existing attachments to our updated record
                    mhr.getAttachmentFiles().addAll(existingRecord.getAttachmentFiles());
                }
            }
            // Set the pet for the medical history
            Pet pet = petsService.getPetById(petId);
            mhr.setPet(pet);

            // In your saveMedicalRecord method, before processing files:
            if (files != null) {
                // Filter out empty files
                files = files.stream()
                        .filter(file -> file != null && !file.isEmpty())
                        .collect(Collectors.toList());
            }

            // Process uploaded files if any
            if (!files.isEmpty()) {
                if (descriptions == null || files.size() != descriptions.size()) {
                    redirectAttributes.addFlashAttribute("message", "Number of files and descriptions must match");
                    return "redirect:/customers/" + urlCustomerId + "/pets/" + petId + "/medicalhistory";
                }

                for (int i = 0; i < files.size(); i++) {
                    MultipartFile file = files.get(i);
                    if (file != null && !file.isEmpty()) {
                        String description = descriptions.get(i);
                        try {
                            // Store the file and get the filename
                            String fileName = fileStorageService.storeFile(file, petId, ImageType.MEDICAL_HISTORY);

                            // Create and add the medical attachment
                            MedicalAttachment attachment = new MedicalAttachment();
                            attachment.setFileName(fileName);
                            attachment.setDescription(description);
                            attachment.setFileType(file.getContentType());
                            mhr.addAttachment(attachment);
                        } catch (Exception e) {
                            e.printStackTrace();
                            redirectAttributes.addFlashAttribute("message",
                                    "Error processing file " + file.getOriginalFilename() + ": " + e.getMessage());
                        }
                    }
                }
            }
            // Save medical record (which will cascade save the attachments)
            medicalHistoryService.saveMedicalHistory(mhr);
            redirectAttributes.addFlashAttribute("message", "Report has been saved successfully!");
            return "redirect:/customers/" + urlCustomerId + "/pets/" + petId + "/medicalhistory";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to save medical record: " + e.getMessage());
            return "redirect:/customers/" + urlCustomerId + "/pets/" + petId + "/medicalhistory";
        }
    }

    @GetMapping("/medicalhistory/delete/{id}")
    @Transactional
    public String deleteMedicalHistory(
            @PathVariable("id") Integer recordId,
            RedirectAttributes redirectAttributes) {
        try {
            // Find the record to determine redirect target
            MedicalHistory mhr = medicalHistoryService.getMedicalHistoryById(recordId);
            if (mhr == null) {
                redirectAttributes.addFlashAttribute("message", "Medical history record not found: " + recordId);
                return "redirect:/customers";
            }
            Pet pet = mhr.getPet();
            Customer customer = pet.getCustomer();

            // Delete the medical history (which will cascade delete attachments)
            medicalHistoryService.deleteMedicalHistory(recordId);
            redirectAttributes.addFlashAttribute("message", "Medical history record deleted successfully.");

            return "redirect:/customers/" + customer.getId() + "/pets/" + pet.getId() + "/medicalhistory";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to delete medical record: " + e.getMessage());
            return "redirect:/customers";
        }
    }

    @GetMapping("/medicalhistory/{id}/attachments/{attachmentId}/download")
    public ResponseEntity<Resource> downloadAttachment(
            @PathVariable("id") Integer medicalHistoryId,
            @PathVariable("attachmentId") Long attachmentId) {
        try {
            // Verify the attachment exists and belongs to the specified medical history
            MedicalAttachment attachment = attachmentService.getAttachmentById(attachmentId)
                    .orElseThrow(() -> new RuntimeException("Attachment not found"));

            if (!attachment.getMedicalHistory().getId().equals(medicalHistoryId)) {
                throw new RuntimeException("Attachment does not belong to the specified medical record");
            }

            // Load the file as a resource
            Resource file = fileStorageService.loadFileAsResource(attachment.getFileName());

            // Determine content type
            String contentType = attachment.getFileType() != null ? attachment.getFileType()
                    : "application/octet-stream";

            // Return the file for download
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + attachment.getFileName() + "\"")
                    .body(file);
        } catch (Exception e) {
            throw new ResponseStatusException(
                    org.springframework.http.HttpStatus.NOT_FOUND,
                    "File not found: " + e.getMessage(), e);
        }
    }

    @DeleteMapping("medicalhistory/attachments/{attachmentId}")
    @ResponseBody
    @Transactional
    public ResponseEntity<?> deleteAttachment(
            @PathVariable("attachmentId") Long attachmentId) {
        try {
            // Verify the attachment exists
            MedicalAttachment attachment = attachmentService.getAttachmentById(attachmentId)
                    .orElseThrow(() -> new RuntimeException("Attachment not found with id: " + attachmentId));

            // Delete the physical file
            fileStorageService.deleteFile(attachment.getFileName());

            // Delete the attachment from database
            attachmentService.deleteAttachment(attachmentId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Attachment deleted successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to delete attachment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

}
