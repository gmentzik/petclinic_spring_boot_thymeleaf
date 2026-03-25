package com.gmentzik.spring.thymeleaf.petclinic.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.net.URLConnection;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.gmentzik.spring.thymeleaf.petclinic.entity.Pet;
import com.gmentzik.spring.thymeleaf.petclinic.common.enums.ImageType;
import com.gmentzik.spring.thymeleaf.petclinic.entity.Customer;
import com.gmentzik.spring.thymeleaf.petclinic.repository.CustomerRepository;
import com.gmentzik.spring.thymeleaf.petclinic.service.PetService;
import com.gmentzik.spring.thymeleaf.petclinic.service.FileStorageService;
import com.gmentzik.spring.thymeleaf.petclinic.service.AnimalTypeService;

@Controller
public class PetController {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PetService petService;

    @Autowired
    private FileStorageService fileStorageService;
    
    @Autowired
    private AnimalTypeService animalTypeService;

/**
 * Handles the deletion of a pet by its ID and redirects to the owner's pet list.
 * 
 * @param id The ID of the pet to be deleted
 * @param model Spring Model (unused in current implementation)
 * @param redirectAttributes For passing success/error messages to the redirected view
 * @return Redirect URL to the owner's pets list page
 * 
 * @implNote This method:
 *           - Retrieves the pet by ID to get the owner's ID
 *           - Deletes the pet using the pet service
 *           - Provides user feedback through flash messages
 *           - Handles exceptions and displays error messages
 *           - Redirects to the owner's pets list after operation
 *           - Maintains data integrity by handling related database constraints
 */
    @GetMapping("/pets/delete/{id}")
    public String deletePet(@PathVariable("id") Integer id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Pet pet = petService.getPetById(id);
            Integer customerId = pet.getCustomer().getId();
            petService.deletePet(id);
            redirectAttributes.addFlashAttribute("message",
                    "Pet with id=" + id + " has been deleted successfully!");
            return "redirect:/customers/" + customerId + "/pets";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", e.getMessage());
            return "redirect:/customers";
        }
    }

/**
 * Displays a form for adding a new pet for a specific customer.
 * 
 * @param customerId The ID of the customer who will own the new pet
 * @param model Spring Model for passing data to the view
 * @return The view name "pet_form" for adding a new pet
 * 
 * @implNote This method:
 *           - Creates a new Pet object and associates it with the specified customer
 *           - Retrieves the customer's details to pre-populate owner information
 *           - Prepares the form with default values and necessary model attributes
 *           - Sets an appropriate page title for the form
 *           - Uses the "pet_form" template for both creating and editing pets
 *           - Expects the customer to exist (throws NoSuchElementException if not found)
 */
    @GetMapping("/customers/{id}/pets/new")
    public String addPet(
            @PathVariable("id") Integer customerId,
            Model model) {
        // Create new author object
        Pet pet = new Pet();
        Customer customer = customerRepository.findById(customerId).get();
        pet.setCustomer(customer);

        model.addAttribute("customer", customer);
        model.addAttribute("pet", pet);
        model.addAttribute("pageTitle", "Create new Pet");
        model.addAttribute("animalTypes", animalTypeService.getAllAnimalTypes());
        model.addAttribute("genders", Pet.Gender.values());
        model.addAttribute("neuteredOptions", Pet.Neutered.values());

        return "pet_form";
    }

/**
 * Handles the creation or update of a pet's information, including photo uploads.
 * 
 * @param pet The Pet object containing the pet's data from the form
 * @param urlCustomerId The ID of the customer who owns the pet (from URL)
 * @param photoFile Optional uploaded photo file for the pet
 * @param redirectAttributes For passing success/error messages to the redirected view
 * @return Redirect URL to the owner's pets list page
 * 
 * @throws RuntimeException If the customer is not found or there's a customer ID mismatch
 * @implNote This method:
 *           - Handles both new pet creation and updates to existing pets
 *           - Manages file uploads for pet photos
 *           - Validates customer ownership before saving
 *           - Updates all pet fields including notes and medical information
 *           - Handles photo file storage and cleanup
 *           - Provides user feedback through flash messages
 *           - Maintains data consistency between pet and owner
 */
    @PostMapping("/customers/{id}/pets/save")
    @Transactional
    public String savePet(
            @ModelAttribute("pet") Pet pet,
            @PathVariable("id") Integer urlCustomerId,
            @RequestParam(value = "photoFile", required = false) MultipartFile photoFile,
            RedirectAttributes redirectAttributes) {
        try {

            System.out.println("SAVE PET");
            // System.out.println(pet);
            // Check if pet has id. If not is a new entry to create 
            // else is existing pet for to update
            if (pet.getId() == null) {
                // New pet
                System.out.println("NEW PET");
                Customer customer = customerRepository.findById(urlCustomerId)
                    .orElseThrow(() -> new RuntimeException("Customer not found"));
                pet.setCustomer(customer);
                
                // Convert name and breed to uppercase
                if (pet.getName() != null) {
                    pet.setName(pet.getName().toUpperCase());
                }
                if (pet.getBreed() != null) {
                    pet.setBreed(pet.getBreed().toUpperCase());
                }
                
                // Save pet first to get an ID
                pet = petService.savePet(pet);
                
                // Handle file upload if present for existing pet
                System.out.println("Photo file: " + (photoFile != null ? photoFile.getOriginalFilename() : "null"));
                if (photoFile != null && !photoFile.isEmpty()) {
                    String fileName = fileStorageService.storeFile(photoFile, pet.getId(), ImageType.PET_ID);
                    pet.setPhotoFilename(fileName);
                    petService.savePet(pet);
                }
            } else {
                System.out.println("EDIT PET");
                // System.out.println("Received pet data: " + pet);
                Pet dbPet = petService.getPetById(pet.getId());
                System.out.println("Database pet before update: " + dbPet);
                // Copy editable fields from form-bound pet to the persistent entity
                if (pet.getName() != null) {
                    dbPet.setName(pet.getName().toUpperCase());
                }
                dbPet.setGender(pet.getGender());
                dbPet.setAnimalType(pet.getAnimalType());
                if (pet.getBreed() != null) {
                    dbPet.setBreed(pet.getBreed().toUpperCase());
                }
                dbPet.setNeutered(pet.getNeutered());
                dbPet.setEntryDate(pet.getEntryDate());
                dbPet.setBirthDate(pet.getBirthDate());
                dbPet.setNote1(pet.getNote1());
                dbPet.setNote2(pet.getNote2());
                dbPet.setNote3(pet.getNote3());
                System.out.println("Database pet after update: " + dbPet);
                Integer objCustomerId = dbPet.getCustomer().getId();
                if (urlCustomerId != objCustomerId) {
                    throw new Exception("customer ID mismatch!!!");
                }
                Pet updatedPet = petService.savePet(dbPet);
                // Handle file upload if present for existing pet
                System.out.println("Photo file: " + (photoFile != null ? photoFile.getOriginalFilename() : "null"));
                if (photoFile != null && !photoFile.isEmpty()) {
                    System.out.println("Processing photo upload...");
                    // Delete old photo if it exists
                    if (dbPet.getPhotoFilename() != null && !dbPet.getPhotoFilename().isEmpty()) {
                        System.out.println("Deleting old photo: " + dbPet.getPhotoFilename());
                        fileStorageService.deleteFile(dbPet.getPhotoFilename());
                    }
                    // Store new photo
                    String fileName = fileStorageService.storeFile(photoFile, updatedPet.getId(), ImageType.PET_ID);
                    System.out.println("Stored new photo: " + fileName);
                    updatedPet.setPhotoFilename(fileName);
                }
                petService.savePet(updatedPet);
                // Pet savedPet = petService.savePet(updatedPet);
                // System.out.println("Pet after save: " + savedPet);
            }
            
            redirectAttributes.addFlashAttribute("message", "Pet saved successfully!");
            return "redirect:/customers/" + urlCustomerId + "/pets";
        } catch (Exception e) {
            redirectAttributes.addAttribute("message", e.getMessage());
            return "redirect:/customers/" + urlCustomerId + "/pets";
        }
    }

/**
 * Displays the pet editing form with the specified pet's data pre-populated.
 * 
 * @param urlCustomerId The ID of the customer who owns the pet (from URL)
 * @param petId The ID of the pet to be edited
 * @param model Spring Model for passing data to the view
 * @param redirectAttributes For passing error messages if needed
 * @return The view name "pet_form" for editing, or redirects to customer's pets list on error
 * 
 * @throws Exception If the pet cannot be found or accessed
 * 
 * @implNote This method:
 *           - Retrieves the pet by ID for editing
 *           - Verifies the pet belongs to the specified customer
 *           - Prepares the pet data for the edit form
 *           - Sets the appropriate page title indicating edit mode
 *           - Uses the same form template as pet creation
 *           - Handles errors by redirecting with an error message
 */
    @GetMapping("/customers/{tId}/pets/{id}/edit")
    public String editPet(
        @PathVariable("tId") Integer urlCustomerId,     
        @PathVariable("id") Integer petId,
         Model model, 
         RedirectAttributes redirectAttributes) {
        try {
            System.out.println("IN EDIT PET");
            Pet pet = petService.getPetById(petId);
            Integer customerId = pet.getCustomer().getId();
            model.addAttribute("customer", pet.getCustomer());
            model.addAttribute("pet", pet);
            model.addAttribute("pageTitle", "Edit Pet ID: " + petId + ",(Customer IDs: "+ customerId +" )");
            model.addAttribute("animalTypes", animalTypeService.getAllAnimalTypes());
            model.addAttribute("genders", Pet.Gender.values());
            model.addAttribute("neuteredOptions", Pet.Neutered.values());
            return "pet_form";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", e.getMessage());
            return "redirect:/customers/" + urlCustomerId + "/pets";
        }
    }

/**
 * Serves a pet's photo file as a web resource with proper content type and headers.
 * 
 * @param filename The name of the file to be served from the storage
 * @return ResponseEntity containing the file as a Resource with appropriate headers
 * 
 * @implNote This method:
 *           - Retrieves the file from storage using the provided filename
 *           - Determines the MIME type of the file based on its extension
 *           - Returns the file with appropriate content type for inline display
 *           - Handles cases where the file is not found (returns 404)
 *           - Uses streaming to efficiently serve files of any size
 *           - Sets the Content-Disposition header to display the file in the browser
 *           - Falls back to "application/octet-stream" if MIME type cannot be determined
 *           - Wraps the response in a ResponseEntity for full HTTP control
 */
    @GetMapping("/pets/photo/{filename:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        try {
            Resource file = fileStorageService.loadFileAsResource(filename);
            String mimeType = URLConnection.guessContentTypeFromName(file.getFilename());
            if (mimeType == null) {
                mimeType = "application/octet-stream";
            }
            
            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mimeType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getFilename() + "\"")
                .body(file);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
