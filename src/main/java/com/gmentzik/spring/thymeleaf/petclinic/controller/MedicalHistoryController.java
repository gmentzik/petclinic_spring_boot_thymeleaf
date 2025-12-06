package com.gmentzik.spring.thymeleaf.petclinic.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.gmentzik.spring.thymeleaf.petclinic.entity.Pet;
import com.gmentzik.spring.thymeleaf.petclinic.entity.MedicalHistory;
import com.gmentzik.spring.thymeleaf.petclinic.common.enums.ImageType;
import com.gmentzik.spring.thymeleaf.petclinic.dto.MedicalImageDto;
import com.gmentzik.spring.thymeleaf.petclinic.entity.Customer;
import com.gmentzik.spring.thymeleaf.petclinic.repository.MedicalHistoryRepository;
import com.gmentzik.spring.thymeleaf.petclinic.service.PetService;
import com.gmentzik.spring.thymeleaf.petclinic.service.FileStorageService;
import com.gmentzik.spring.thymeleaf.petclinic.service.MedicalHistoryService;

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
            System.out.println("Files size: " + files.size());
            System.out.println("Files descriptions size: " + descriptions.size());

            // Set the pet for the medical history
            Pet pet = petsService.getPetById(petId);
            mhr.setPet(pet);

            // Process uploaded files if any
            if (files.size() != descriptions.size()) {
                // Handle error: mismatch in data integrity
                System.err.println("Error: Image and description lists are not the same size.");
            }

            if (files != null && !files.isEmpty()) {
                List<MedicalImageDto> medicalImages = new ArrayList<>();

                for (int i = 0; i < files.size(); i++) {
                    MultipartFile file = files.get(i);
                    if (file != null && !file.isEmpty()) {
                        String description = (descriptions != null && i < descriptions.size()) ? descriptions.get(i)
                                : "";

                        try {
                            // Store the file and get the filename
                            String fileName = fileStorageService.storeFile(file, petId, ImageType.MEDICAL_HISTORY);

                            // Create and add the medical image DTO
                            MedicalImageDto imageDto = new MedicalImageDto();
                            imageDto.setImageName(fileName);
                            imageDto.setImageDescription(description);
                            medicalImages.add(imageDto);
                        } catch (Exception e) {
                            // Log the error but don't fail the entire operation
                            e.printStackTrace();
                        }
                    }
                }

                // Convert the list of images to JSON and store in the attachments field
                if (!medicalImages.isEmpty()) {
                    try {
                        ObjectMapper objectMapper = new ObjectMapper();
                        String attachmentsJson = objectMapper.writeValueAsString(medicalImages);
                        System.out.println("attachmentsJson: " + attachmentsJson);
                        mhr.setAttachments(attachmentsJson);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                        redirectAttributes.addFlashAttribute("error",
                                "Error processing image attachments: " + e.getMessage());
                    }
                }
            }

            // Save medical record
            medicalHistoryService.saveMedicalHistory(mhr);

            redirectAttributes.addFlashAttribute("message", "Report has been saved successfully!");
            return "redirect:/customers/" + urlCustomerId + "/pets/" + petId + "/medicalhistory";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to save medical record: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/customers/" + urlCustomerId + "/pets/" + petId + "/medicalhistory";
        }
    }

    @GetMapping("/medicalhistory/delete/{id}")
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
            Pet pet = mhr.gePet();
            Customer customer = pet.getCustomer();

            medicalHistoryService.deleteMedicalHistory(recordId);
            redirectAttributes.addFlashAttribute("message", "Medical history record deleted successfully.");

            return "redirect:/customers/" + customer.getId() + "/pets/" + pet.getId() + "/medicalhistory";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", e.getMessage());
            return "redirect:/customers";
        }
    }

}
