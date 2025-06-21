package com.gmentzik.spring.thymeleaf.petclinic.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
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

import com.gmentzik.spring.thymeleaf.petclinic.entity.Pet;
import com.gmentzik.spring.thymeleaf.petclinic.entity.Customer;
import com.gmentzik.spring.thymeleaf.petclinic.repository.CustomerRepository;
import com.gmentzik.spring.thymeleaf.petclinic.repository.PetRepository;


@Controller
public class CustomerController {

  @Autowired
  private CustomerRepository customerRepository;

  @Autowired
  private PetRepository petRepository;



  @GetMapping("/customers")
  public String getAll(
      Model model, 
      @RequestParam(required = false) String keyword,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "6") int size,
      @RequestParam(defaultValue = "id,asc") String[] sort) {
    try {

      List<Customer> customers = new ArrayList<Customer>();
      String sortField = sort[0];
      String sortDirection = sort[1];

      Direction direction = sortDirection.equals("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
      Order order = new Order(direction, sortField);

      Pageable pageable = PageRequest.of(page - 1, size, Sort.by(order));

      Page<Customer> pageTuts;
      if (keyword == null) {
        pageTuts = customerRepository.findAll(pageable);
      } else {
        pageTuts = customerRepository.findByFirstNameContainingIgnoreCaseOrSurNameContainingIgnoreCase(keyword,keyword, pageable);
        model.addAttribute("keyword", keyword);
      }

      customers = pageTuts.getContent();

      // Initialize authors to avoid LazyInitializationException
      for (Customer customer : customers) {
        customer.getPets().size();
      }

      model.addAttribute("customers", customers);
      model.addAttribute("currentPage", pageTuts.getNumber() + 1);
      model.addAttribute("totalItems", pageTuts.getTotalElements());
      model.addAttribute("totalPages", pageTuts.getTotalPages());
      model.addAttribute("pageSize", size);
      model.addAttribute("sortField", sortField);
      model.addAttribute("sortDirection", sortDirection);
      model.addAttribute("reverseSortDirection", sortDirection.equals("asc") ? "desc" : "asc");
    } catch (Exception e) {
      model.addAttribute("message", e.getMessage());
    }

    return "customers";
  }

  @GetMapping("/customers/new")
  public String addCustomer(Model model) {
    Customer customer = new Customer();
    customer.setPublished(true);

    model.addAttribute("customer", customer);
    model.addAttribute("pageTitle", "Create new Customer");

    return "customer_form";
  }

  @PostMapping("/customers/save")
  public String saveCustomer(Customer customer, RedirectAttributes redirectAttributes) {
    try {
      System.out.println("Saving customer: " + customer);
      
      if (customer.getId() != null) {
        // This is an update operation
        Customer existingCustomer = customerRepository.findById(customer.getId())
            .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        // Update the fields but keep the pets collection
        existingCustomer.setFirstName(customer.getFirstName());
        existingCustomer.setSurName(customer.getSurName());
        existingCustomer.setLevel(customer.getLevel());
        existingCustomer.setEmail(customer.getEmail());
        existingCustomer.setPhone(customer.getPhone());
        existingCustomer.setAddress(customer.getAddress());
        existingCustomer.setCity(customer.getCity());
        existingCustomer.setState(customer.getState());
        existingCustomer.setZipCode(customer.getZipCode());
        existingCustomer.setPublished(customer.isPublished());
        
        customer = existingCustomer;
      }
      
      Customer savedCustomer = customerRepository.save(customer);
      System.out.println("Saved customer: " + savedCustomer);

      redirectAttributes.addFlashAttribute("message", "Customer has been saved successfully!");
    } catch (Exception e) {
      System.err.println("Error saving customer: " + e.getMessage());
      e.printStackTrace();
      redirectAttributes.addFlashAttribute("message", e.getMessage());
    }

    return "redirect:/customers";
  }

  @GetMapping("/customers/{id}")
  public String editCustomer(@PathVariable("id") Integer id, Model model, RedirectAttributes redirectAttributes) {
    try {
      Customer customer = customerRepository.findById(id).get();

      model.addAttribute("customer", customer);
      model.addAttribute("pageTitle", "Edit Customer (ID: " + id + ")");

      return "customer_form";
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("message", e.getMessage());

      return "redirect:/customers";
    }
  }

  @GetMapping("/customers/delete/{id}")
  public String deleteCustomer(@PathVariable("id") Integer id, Model model, RedirectAttributes redirectAttributes) {
    try {
      customerRepository.deleteById(id);

      redirectAttributes.addFlashAttribute("message", "Customer with id=" + id + " has been deleted successfully!");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("message", e.getMessage());
    }

    return "redirect:/customers";
  }

  @GetMapping("/customers/{id}/published/{status}")
  public String updateCustomerPublishedStatus(@PathVariable("id") Integer id, @PathVariable("status") boolean published,
      Model model, RedirectAttributes redirectAttributes) {
    try {
      customerRepository.updatePublishedStatus(id, published);

      String status = published ? "published" : "disabled";
      String message = "Customer id=" + id + " has been " + status;

      redirectAttributes.addFlashAttribute("message", message);
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("message", e.getMessage());
    }

    return "redirect:/customers";
  }

 
  @GetMapping("/customers/{id}/pets")
  public String getPets(
      Model model,
      @PathVariable("id") Integer id,
      @RequestParam(required = false) String keyword,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "6") int size,
      @RequestParam(defaultValue = "id,asc") String[] sort) {

    try {
      Optional<Customer> customerData = customerRepository.findById(id);
      System.out.println(">>>>>>>>>   GET PETS");
      System.out.println(customerData);
      System.out.println(customerData.isPresent());
      if (customerData.isPresent()) {
        Customer customer = customerData.get();
        System.out.println("customer");
        
        String sortField = sort[0];
        String sortDirection = sort[1];

        Direction direction = sortDirection.equals("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Order order = new Order(direction, sortField);

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(order));
// Test        Pageable pageable = PageRequest.of(0, 6);

        System.out.println("pageable");
        System.out.println(pageable);
        System.out.println("pageable"); 
        Page<Pet> pagePets;
        pagePets = petRepository.findByCustomer(customer, pageable);

        model.addAttribute("customer", customer);
        model.addAttribute("pets", pagePets.getContent());
        model.addAttribute("customerTitle", customer.getFirstName() + " " + customer.getSurName());
        model.addAttribute("currentPage", pagePets.getNumber() + 1);
        model.addAttribute("totalItems", pagePets.getTotalElements());
        model.addAttribute("totalPages", pagePets.getTotalPages());
        model.addAttribute("pageSize", size);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDirection", sortDirection);
        model.addAttribute("reverseSortDirection", sortDirection.equals("asc") ? "desc" : "asc");

      } else {
        model.addAttribute("message", "Customer not found");
      }
    } catch (Exception e) {
      model.addAttribute("message", e.getMessage());
    }
    return "pets";

  }


}
