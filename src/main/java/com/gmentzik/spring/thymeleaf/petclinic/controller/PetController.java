package com.gmentzik.spring.thymeleaf.petclinic.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.gmentzik.spring.thymeleaf.petclinic.entity.Pet;
import com.gmentzik.spring.thymeleaf.petclinic.entity.Customer;
import com.gmentzik.spring.thymeleaf.petclinic.repository.CustomerRepository;
import com.gmentzik.spring.thymeleaf.petclinic.service.PetService;

@Controller
public class PetController {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PetService petService;

    // Delete pet and redirect to customer pets list
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

    // Returns new pet form with customer data a
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

        return "pet_form";
    }

    // Save new pet or edit existing pet 
    // and redirect to customer pets listS
    @PostMapping("/customers/{id}/pets/save")
    public String savePet(Pet pet,
                            @PathVariable("id") Integer urlCustomerId,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        try {

            System.out.println("SAVE PET");
            System.out.println(pet);
            // Tutorial authorTutorial = author.getTutorial();
            // If new Author/Pet does not contain Tutorial obj
            // if (authorTutorial == null) {
            if (pet.getId() == null) {
                System.out.println("NEW PET");
                Customer customer = customerRepository.findById(urlCustomerId).get();
                pet.setCustomer(customer);
                petService.savePet(pet);
            } else {
                System.out.println("EDIT PET");
                Pet dbPet = petService.getPetById(pet.getId());
                // Copy editable fields from form-bound pet to the persistent entity
                dbPet.setName(pet.getName());
                dbPet.setGender(pet.getGender());
                dbPet.setAnimalType(pet.getAnimalType());
                dbPet.setBreed(pet.getBreed());
                dbPet.setNeutered(pet.getNeutered());
                dbPet.setEntryDate(pet.getEntryDate());
                dbPet.setBirthDate(pet.getBirthDate());
                dbPet.setNote1(pet.getNote1());
                dbPet.setNote2(pet.getNote2());
                dbPet.setNote3(pet.getNote3());
                Integer objTutorialId = dbPet.getCustomer().getId();
                if (urlCustomerId != objTutorialId) {
                    throw new Exception("customer ID mismatch!!!");
                }
                petService.savePet(dbPet);
            }
            
            redirectAttributes.addFlashAttribute("message", "Pet saved successfully!");
            return "redirect:/customers/" + urlCustomerId + "/pets";
        } catch (Exception e) {
            redirectAttributes.addAttribute("message", e.getMessage());
            return "redirect:/customers/" + urlCustomerId + "/pets";
        }
    }

    // Returns edit pet form with pet data 
    // when edit button is clicked at pet list
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
            return "pet_form";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", e.getMessage());
            return "redirect:/customers/" + urlCustomerId + "/pets";
        }
    }

 
}
