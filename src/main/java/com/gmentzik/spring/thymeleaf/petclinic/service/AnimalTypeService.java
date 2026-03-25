package com.gmentzik.spring.thymeleaf.petclinic.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
public class AnimalTypeService {
    
    @Value("${app.locale}")
    private String defaultLocale;
    
    private Map<String, Map<String, String>> animalTypes;
    
    @PostConstruct
    public void init() {
        animalTypes = new HashMap<>();
        
        // Initialize with static data
        addAnimalType("DOG", "GR", "ΣΚΥΛΟΣ");
        addAnimalType("DOG", "EN", "Dog");
        
        addAnimalType("CAT", "GR", "ΓΑΤΑ");
        addAnimalType("CAT", "EN", "Cat");
        
        addAnimalType("RODDENT", "GR", "ΤΡΩΚΤΙΚΟ");
        addAnimalType("RODDENT", "EN", "Rodent");
        
        addAnimalType("BIRD", "GR", "ΠΤΗΝΟ");
        addAnimalType("BIRD", "EN", "Bird");
        
        addAnimalType("FISH", "GR", "ΨΑΡΙ");
        addAnimalType("FISH", "EN", "Fish");
        
        addAnimalType("REPTILE", "GR", "ΕΡΠΕΤΟ");
        addAnimalType("REPTILE", "EN", "Reptile");
        
        addAnimalType("OTHER", "GR", "ΑΛΛΟ");
        addAnimalType("OTHER", "EN", "Other");
    }
    
    private void addAnimalType(String code, String locale, String displayName) {
        animalTypes.computeIfAbsent(code, k -> new HashMap<>()).put(locale, displayName);
    }
    
    /**
     * Gets display name for animal type in specified language
     */
    public String getDisplayName(String key, String language) {
        if (key == null || !animalTypes.containsKey(key)) {
            return key;
        }
        
        Map<String, String> names = animalTypes.get(key);
        return names.getOrDefault(language, key);
    }
    
    /**
     * Gets display name for animal type in default locale
     */
    public String getDisplayName(String key) {
        return getDisplayName(key, defaultLocale);
    }
    
    /**
     * Gets all animal types for default locale
     */
    public Map<String, String> getAllAnimalTypes() {
        return getAllAnimalTypes(defaultLocale);
    }
    
    /**
     * Gets all animal types for specified language
     */
    public Map<String, String> getAllAnimalTypes(String language) {
        Map<String, String> result = new HashMap<>();
        
        for (Map.Entry<String, Map<String, String>> entry : animalTypes.entrySet()) {
            String code = entry.getKey();
            String displayName = entry.getValue().getOrDefault(language, code);
            result.put(code, displayName);
        }
        
        return result;
    }
    
    /**
     * Gets raw animal types map with all languages
     */
    public Map<String, Map<String, String>> getAllAnimalTypesRaw() {
        return new HashMap<>(animalTypes);
    }
    
    /**
     * Validates if animal type code exists
     */
    public boolean isValidAnimalType(String key) {
        return key != null && animalTypes.containsKey(key);
    }
    
    /**
     * Gets all supported language codes
     */
    public Set<String> getSupportedLanguages() {
        Set<String> languages = new java.util.HashSet<>();
        
        for (Map<String, String> names : animalTypes.values()) {
            languages.addAll(names.keySet());
        }
        
        return languages;
    }
}
