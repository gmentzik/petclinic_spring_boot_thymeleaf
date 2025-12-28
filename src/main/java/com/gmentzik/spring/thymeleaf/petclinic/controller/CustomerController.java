package com.gmentzik.spring.thymeleaf.petclinic.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
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


/**
 * Retrieves a paginated and sortable list of customers, with optional keyword search.
 * 
 * @param model Spring Model for passing data to the view
 * @param keyword Optional search term to filter customers by first name or surname (case-insensitive)
 * @param page Page number for pagination (1-based, default: 1)
 * @param size Number of items per page (default: 6)
 * @param sort Array containing sort field and direction (default: ["id","asc"])
 * @return The view name "customers" to render the customer list
 * 
 * @implNote This method:
 *           - Supports pagination and sorting
 *           - Implements case-insensitive search across first name and surname
 *           - Initializes lazy-loaded pet collections to prevent LazyInitializationException
 *           - Adds pagination and sorting metadata to the model
 *           - Handles exceptions gracefully by adding error messages to the model
 */
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

/**
 * Displays a form for creating a new customer.
 * 
 * @param model Spring Model for passing data to the view
 * @return The view name "customer_form" for rendering the customer creation form
 * 
 * @implNote This method:
 *           - Initializes a new Customer object with default values
 *           - Sets the customer as active by default
 *           - Prepares the model with necessary attributes for the form
 *           - Uses the "customer_form" template for both create and update operations
 *           - Sets the appropriate page title for customer creation
 */
  @GetMapping("/customers/new")
  public String addCustomer(Model model) {
    Customer customer = new Customer();
    customer.setActive(true);

    model.addAttribute("customer", customer);
    model.addAttribute("pageTitle", "Create new Customer");

    return "customer_form";
  }

/**
 * Handles the creation or update of a customer record.
 * 
 * @param customer The Customer object containing the customer data to be saved
 * @param redirectAttributes For passing flash messages to the redirected view
 * @return Redirect URL to the customers list page
 * 
 * @throws RuntimeException If the customer being updated is not found in the database
 * 
 * @implNote This method:
 *           - Supports both creating new customers and updating existing ones
 *           - Preserves the existing pet associations during updates
 *           - Updates all customer fields including notes and contact information
 *           - Provides user feedback through flash messages
 *           - Handles exceptions and redirects with appropriate error messages
 *           - Logs the save operation for debugging purposes
 */
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
        existingCustomer.setEmail(customer.getEmail());
        existingCustomer.setPhone(customer.getPhone());
        existingCustomer.setAddress(customer.getAddress());
        existingCustomer.setCity(customer.getCity());
        existingCustomer.setState(customer.getState());
        existingCustomer.setZipCode(customer.getZipCode());
        existingCustomer.setActive(customer.isActive());
        existingCustomer.setEntryDate(customer.getEntryDate());
        existingCustomer.setNote1(customer.getNote1());
        existingCustomer.setNote2(customer.getNote2());
        existingCustomer.setNote3(customer.getNote3());
        
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

/**
 * Displays the customer edit form with the specified customer's data pre-populated.
 * 
 * @param id The ID of the customer to be edited
 * @param model Spring Model for passing data to the view
 * @param redirectAttributes For passing flash messages in case of errors
 * @return The view name "customer_form" for editing, or redirects to customers list on error
 * 
 * @throws NoSuchElementException If no customer exists with the given ID
 * 
 * @implNote This method:
 *           - Retrieves the customer by ID for editing
 *           - Prepares the customer data for the edit form
 *           - Sets the appropriate page title indicating edit mode
 *           - Uses the same form template as customer creation
 *           - Handles potential errors during customer retrieval
 *           - Maintains consistency with the addCustomer method's form handling
 */
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

/**
 * Handles the deletion of a customer by their ID.
 * 
 * @param id The ID of the customer to be deleted
 * @param model Spring Model (unused in current implementation)
 * @param redirectAttributes For passing success/error messages to the redirected view
 * @return Redirect URL to the customers list page
 * 
 * @implNote This method:
 *           - Deletes the customer with the specified ID from the database
 *           - Provides user feedback through flash messages
 *           - Handles exceptions gracefully with appropriate error messages
 *           - Maintains data integrity by handling related database constraints
 *           - Redirects to the customers list page after operation
 */
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

/**
 * Retrieves and displays a paginated list of pets belonging to a specific customer.
 * 
 * @param model Spring Model for passing data to the view
 * @param id The ID of the customer whose pets are being retrieved
 * @param keyword Optional search term for filtering pets (unused in current implementation)
 * @param page Page number for pagination (1-based, default: 1)
 * @param size Number of items per page (default: 6)
 * @param sort Array containing sort field and direction (default: ["id","asc"])
 * @return The view name "pets" to render the pet list
 * 
 * @implNote This method:
 *           - Retrieves the customer by ID
 *           - Fetches a paginated and sortable list of the customer's pets
 *           - Adds pagination and sorting metadata to the model
 *           - Handles cases where the customer is not found
 *           - Includes customer information for display in the view
 *           - Returns a dedicated view for displaying pets with navigation controls
 */
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
