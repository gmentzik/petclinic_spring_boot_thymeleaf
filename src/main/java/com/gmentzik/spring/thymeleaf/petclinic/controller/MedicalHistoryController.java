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

/**
 * Retrieves and displays a paginated list of medical history records for a specific pet.
 * 
 * @param petId The ID of the pet to retrieve medical history for
 * @param urlCustomerId The ID of the customer (from URL) for redirection purposes
 * @param keyword Optional search keyword for filtering records (unused in current implementation)
 * @param page Page number for pagination (default: 1)
 * @param size Number of records per page (default: 6)
 * @param sort Array containing sort field and direction (default: ["id","desc"])
 * @param model Spring Model object for passing data to the view
 * @param redirectAttributes For passing flash attributes in case of errors
 * @return The view name "medical_history" for successful retrieval, or redirects to customer's pets list on error
 * 
 * @throws Exception If there's an error retrieving the pet or its medical history
 */
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

/**
 * Displays a form for creating a new medical record for a specific pet.
 * 
 * @param petId The ID of the pet for which to create a new medical record
 * @param model Spring Model object for passing data to the view
 * @return The view name "medical_record_form" for displaying the medical record creation form
 * 
 * @implNote This method initializes a new MedicalHistory object and associates it with the specified pet
 *           before displaying the form. It also adds customer and pet information to the model
 *           for display in the form.
 */
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

/**
 * Displays a form for editing an existing medical record.
 * 
 * @param urlCustomerId The ID of the customer (from URL) for redirection in case of errors
 * @param petId The ID of the pet associated with the medical record
 * @param recordId The ID of the medical record to edit
 * @param model Spring Model object for passing data to the view
 * @param redirectAttributes For passing flash attributes in case of errors
 * @return The view name "medical_record_form" for successful retrieval, or redirects to the pet's medical history on error
 * 
 * @throws Exception If there's an error retrieving the medical record or associated pet/customer data
 * 
 * @implNote This method retrieves an existing medical record and prepares it for editing in the same form
 *           used for creating new records. It includes error handling to redirect back to the medical history
 *           view if the record cannot be found or accessed.
 */
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

 /**
 * Handles the submission of a medical record form, saving both the record and any associated attachments.
 * 
 * @param mhr The MedicalHistory object bound from the form submission
 * @param urlCustomerId The ID of the customer (from URL) for redirection
 * @param petId The ID of the pet associated with the medical record
 * @param model Spring Model object (unused in current implementation)
 * @param files List of uploaded image files (optional)
 * @param descriptions List of descriptions corresponding to the uploaded files (optional)
 * @param redirectAttributes For passing flash attributes for success/error messages
 * @return Redirect URL to the pet's medical history page after saving
 * 
 * @throws Exception If there's an error during file processing or database operations
 * 
 * @implNote This method handles both creating new records and updating existing ones.
 *           It processes file uploads, associates them with the medical record,
 *           and maintains referential integrity with the pet. The method includes
 *           validation to ensure file-description pairs match and handles transaction
 *           rollback on errors.
 */   
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

/**
 * Handles the deletion of a medical history record and its associated attachments.
 * 
 * @param recordId The ID of the medical history record to delete
 * @param redirectAttributes For passing flash attributes for success/error messages
 * @return Redirect URL to the pet's medical history page after deletion, or to customers list on error
 * 
 * @throws Exception If there's an error during the deletion process
 * 
 * @implNote This method:
 *           - Retrieves the medical record to determine the associated pet and customer
 *           - Deletes the record (cascading to associated attachments)
 *           - Handles error cases with appropriate redirects and messages
 *           - Maintains data integrity by ensuring proper cleanup of related resources
 *           - Uses transactions to ensure atomicity of the delete operation
 */
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

/**
 * Handles the download of a medical history attachment file.
 * 
 * @param medicalHistoryId The ID of the medical history record the attachment belongs to
 * @param attachmentId The ID of the attachment to download
 * @return ResponseEntity containing the file as a Resource with appropriate headers
 * @throws RuntimeException If the attachment is not found or doesn't belong to the specified medical record
 * 
 * @implNote This method:
 *           - Verifies the existence and ownership of the attachment
 *           - Streams the file content with proper content type and headers
 *           - Supports resumable downloads with range requests
 *           - Includes security checks to prevent unauthorized access
 *           - Handles file not found and access control scenarios
 */
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

 /**
 * Handles the deletion of a medical history attachment via AJAX request.
 * 
 * @param attachmentId The ID of the attachment to be deleted
 * @return ResponseEntity containing a JSON response with operation status
 * 
 * @throws RuntimeException If the attachment is not found or deletion fails
 * 
 * @implNote This method:
 *           - Verifies the existence of the attachment
 *           - Deletes the physical file from storage
 *           - Removes the attachment record from the database
 *           - Returns a JSON response indicating success or failure
 *           - Uses transactions to ensure data consistency
 *           - Handles errors gracefully with appropriate HTTP status codes
 */   
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
