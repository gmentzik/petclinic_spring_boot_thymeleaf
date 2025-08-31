package com.gmentzik.spring.thymeleaf.petclinic.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gmentzik.spring.thymeleaf.petclinic.dto.AIContext;
import com.gmentzik.spring.thymeleaf.petclinic.service.AIContextService;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

@Controller
@RequestMapping("/ollama")
public class OllamaController {

    private final String OLLAMA_API_URL = "http://localhost:11434/api/generate";
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AIContextService aiContextService;

    public OllamaController(AIContextService aiContextService) {
        this.aiContextService = aiContextService;
    }

    @GetMapping("")
    public String showChatPage(Model model) {
        return "ollama/chat";
    }

    @GetMapping("/ask")
    public String askQuestion(@RequestParam String question, Model model) {
        try {
            // Set up headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // Get database context
            AIContext context = aiContextService.getContextForQuestion(question);
            
            // Create system prompt with database context
            String systemPrompt = "You are a helpful assistant for a pet clinic. " +
                "You have access to the following information about customers and their pets. " +
                "Use this information to answer questions accurately.\n\n" +
                "CUSTOMER INFORMATION:\n" + context.getCustomerInfo() + "\n\n" +
                "When answering questions about customers or pets, please be precise and use the information provided. " +
                "If you're asked about something not in the provided information, say you don't have that information.\n\n" +
                "Question: " + question;
            
            // Create request body
            String requestBody = String.format(
                "{\"model\": \"llama3\", \"prompt\": \"%s\", \"stream\": false}",
                systemPrompt.replace("\"", "\\\"").replace("\n", "\\n")
            );
            
            // Create request entity
            HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
            
            // Make the request
            ResponseEntity<String> response = restTemplate.exchange(
                OLLAMA_API_URL,
                HttpMethod.POST,
                request,
                String.class
            );
            
            // Parse the response
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode responseJson = objectMapper.readTree(response.getBody());
                String responseText = responseJson.path("response").asText();
                model.addAttribute("response", responseText);
            } else {
                model.addAttribute("error", "Error from Ollama API: " + response.getStatusCode());
            }
        } catch (Exception e) {
            model.addAttribute("error", "Error connecting to Ollama API: " + e.getMessage());
        }
        model.addAttribute("question", question);
        return "ollama/chat";
    }
}
