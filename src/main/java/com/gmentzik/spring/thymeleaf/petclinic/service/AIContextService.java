package com.gmentzik.spring.thymeleaf.petclinic.service;

import com.gmentzik.spring.thymeleaf.petclinic.dto.AIContext;
import com.gmentzik.spring.thymeleaf.petclinic.entity.Customer;
import com.gmentzik.spring.thymeleaf.petclinic.entity.Pet;
import com.gmentzik.spring.thymeleaf.petclinic.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AIContextService {

    @Autowired
    private CustomerRepository customerRepository;

    public AIContext getContextForQuestion(String question) {
        AIContext context = new AIContext();
        
        // Get all customers and their pets
        List<Customer> customers = customerRepository.findAll();
        
        // Build customer info string
        String customerInfo = customers.stream()
            .map(customer -> {
                int petCount = customer.getPets() != null ? customer.getPets().size() : 0;
                String petsList = "";
                if (customer.getPets() != null && !customer.getPets().isEmpty()) {
                    petsList = " Pets: " + customer.getPets().stream()
                        .map(Pet::getName)
                        .collect(Collectors.joining(", "));
                }
                return String.format("Customer ID: %d, Name: %s %s, Phone: %s, Email: %s, Number of Pets: %d%s",
                    customer.getId(),
                    customer.getFirstName(),
                    customer.getSurName() != null ? customer.getSurName() : "",
                    customer.getPhone() != null ? customer.getPhone() : "N/A",
                    customer.getEmail() != null ? customer.getEmail() : "N/A",
                    petCount,
                    petsList);
            })
            .collect(Collectors.joining("\n"));
        
        context.setCustomerInfo(customerInfo);
        
        // Add more context as needed (e.g., pet info, medical history)
        
        return context;
    }
}
