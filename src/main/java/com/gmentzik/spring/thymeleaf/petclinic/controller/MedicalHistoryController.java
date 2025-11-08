package com.gmentzik.spring.thymeleaf.petclinic.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.multipart.MultipartFile;

import com.gmentzik.spring.thymeleaf.petclinic.entity.Pet;
import com.gmentzik.spring.thymeleaf.petclinic.entity.MedicalHistory;
import com.gmentzik.spring.thymeleaf.petclinic.entity.Customer;
import com.gmentzik.spring.thymeleaf.petclinic.repository.MedicalHistoryRepository;
import com.gmentzik.spring.thymeleaf.petclinic.service.PetService;
import com.gmentzik.spring.thymeleaf.petclinic.service.MedicalHistoryService;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
public class MedicalHistoryController {

    @Autowired
    private PetService petsService;

    @Autowired
    private MedicalHistoryService medicalHistoryService;

    @Autowired
    private MedicalHistoryRepository medicalHistoryRepository;

    @Value("${medicalhistory.attachments-dir}")
    private String attachmentsDir;

    private static final long MAX_FILE_SIZE_BYTES = 1_000_000L; // 1MB

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
    // Test        Pageable pageable = PageRequest.of(0, 6);
    
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
    
            model.addAttribute("pageTitle", "Medical History for Pet ID: " + petId+ ",(Customer IDs: "+ customerId +" )");
            return "medical_history"; // Assuming there is a view named "medical_history_view" to display the medical history

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
            Pet pet = mhr.getPet();
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
    public String saveMedicalRecord(MedicalHistory mhr,
                            @PathVariable("cId") Integer urlCustomerId,                        
                            @PathVariable("pId") Integer petId,
                            @RequestParam(name = "images", required = false) List<MultipartFile> images,
                            @RequestParam(name = "descriptions", required = false) List<String> descriptions,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        try {

            System.out.println("SAVE MEDICAL HISTORY REPORT");
            System.out.println(mhr);

            if (mhr.getId() == null) {
                System.out.println("NEW MEDICAL HISTORY REPORT");
                Pet pet = petsService.getPetById(petId);
                mhr.setPet(pet);
                handleAndPersistAttachments(petId, mhr, images, descriptions, redirectAttributes);
                medicalHistoryService.saveMeddicalHistory(mhr);
            } else {
                System.out.println("EDIT MEDICAL RECORD");
                Pet dbPet = petsService.getPetById(petId);
                mhr.setPet(dbPet);
                handleAndPersistAttachments(petId, mhr, images, descriptions, redirectAttributes);
                medicalHistoryService.saveMeddicalHistory(mhr);
            }
            
            redirectAttributes.addFlashAttribute("message", "Report has been saved successfully!");
            return "redirect:/customers/" + urlCustomerId + "/pets/" + petId + "/medicalhistory";
        } catch (Exception e) {
            redirectAttributes.addAttribute("message", e.getMessage());
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
            Pet pet = mhr.getPet();
            Customer customer = pet.getCustomer();

            medicalHistoryService.deleteMedicalHistory(recordId);
            redirectAttributes.addFlashAttribute("message", "Medical history record deleted successfully.");

            return "redirect:/customers/" + customer.getId() + "/pets/" + pet.getId() + "/medicalhistory";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", e.getMessage());
            return "redirect:/customers";
        }
    }

    private void handleAndPersistAttachments(Integer petId, MedicalHistory mhr, List<MultipartFile> images, List<String> descriptions, RedirectAttributes redirectAttributes) throws IOException {
        if (images == null || images.isEmpty()) {
            return;
        }

        Path baseDir = Paths.get(attachmentsDir).toAbsolutePath().normalize();
        Files.createDirectories(baseDir);

        List<Map<String, String>> existing = new ArrayList<>();
        try {
            if (mhr.getAttachments() != null && mhr.getAttachments().trim().length() > 0) {
                existing = new ObjectMapper().readValue(mhr.getAttachments(), new TypeReference<List<Map<String, String>>>() {});
            }
        } catch (Exception ex) {
            existing = new ArrayList<>();
        }

        List<Map<String, String>> toAppend = new ArrayList<>();
        for (int i = 0; i < images.size(); i++) {
            MultipartFile file = images.get(i);
            if (file == null || file.isEmpty()) continue;

            if (file.getSize() > MAX_FILE_SIZE_BYTES) {
                redirectAttributes.addFlashAttribute("message", "One or more files exceed the 1MB size limit and were skipped.");
                continue;
            }

            String original = file.getOriginalFilename();
            String ext = "";
            if (original != null && original.contains(".")) {
                ext = original.substring(original.lastIndexOf('.'));
            }
            String storedName = petId + "-" + UUID.randomUUID() + ext;
            Path target = baseDir.resolve(storedName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            String desc = (descriptions != null && i < descriptions.size()) ? descriptions.get(i) : "";
            Map<String, String> item = new HashMap<String, String>();
            item.put("description", desc == null ? "" : desc);
            item.put("fileName", storedName);
            toAppend.add(item);
        }

        if (!toAppend.isEmpty()) {
            existing.addAll(toAppend);
            String json = new ObjectMapper().writeValueAsString(existing);
            mhr.setAttachments(json);
        }
    }

}
