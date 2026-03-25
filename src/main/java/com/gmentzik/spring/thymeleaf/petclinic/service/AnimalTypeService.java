package com.gmentzik.spring.thymeleaf.petclinic.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnimalTypeService {
    
    @Value("${app.locale}")
    private String defaultLocale;
    
    private List<AnimalType> animalTypes;
    
    private static class AnimalType {
        private String code;
        private int order;
        private Map<String, String> displayNames;
        
        public AnimalType(String code, int order) {
            this.code = code;
            this.order = order;
            this.displayNames = new HashMap<>();
        }
        
        public AnimalType addDisplayName(String language, String name) {
            this.displayNames.put(language, name);
            return this;
        }
        
        public String getCode() {
            return code;
        }
        
        public int getOrder() {
            return order;
        }
        
        public String getDisplayName(String language) {
            return displayNames.getOrDefault(language, code);
        }
        
        public Map<String, String> getAllDisplayNames() {
            return new HashMap<>(displayNames);
        }
    }
    
    @PostConstruct
    public void init() {
        animalTypes = new ArrayList<>();
        
        // Initialize with static data
        animalTypes.add(new AnimalType("DOG", 1)
            .addDisplayName("GR", "ΣΚΥΛΟΣ")
            .addDisplayName("EN", "Dog"));
        
        animalTypes.add(new AnimalType("CAT", 2)
            .addDisplayName("GR", "ΓΑΤΑ")
            .addDisplayName("EN", "Cat"));
        
        animalTypes.add(new AnimalType("RODDENT", 3)
            .addDisplayName("GR", "ΤΡΩΚΤΙΚΟ")
            .addDisplayName("EN", "Rodent"));
        
        animalTypes.add(new AnimalType("BIRD", 4)
            .addDisplayName("GR", "ΠΤΗΝΟ")
            .addDisplayName("EN", "Bird"));
        
        animalTypes.add(new AnimalType("FISH", 5)
            .addDisplayName("GR", "ΨΑΡΙ")
            .addDisplayName("EN", "Fish"));
        
        animalTypes.add(new AnimalType("REPTILE", 6)
            .addDisplayName("GR", "ΕΡΠΕΤΟ")
            .addDisplayName("EN", "Reptile"));
        
        animalTypes.add(new AnimalType("OTHER", 100)
            .addDisplayName("GR", "ΑΛΛΟ")
            .addDisplayName("EN", "Other"));
    }
    
    /**
     * Gets display name for animal type in specified language
     */
    public String getDisplayName(String code, String language) {
        AnimalType animalType = findByCode(code);
        return animalType != null ? animalType.getDisplayName(language) : code;
    }
    
    /**
     * Gets display name for animal type in default locale
     */
    public String getDisplayName(String code) {
        return getDisplayName(code, defaultLocale);
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
        return animalTypes.stream()
            .sorted(Comparator.comparing(AnimalType::getOrder))
            .collect(Collectors.toMap(
                AnimalType::getCode,
                at -> at.getDisplayName(language),
                (oldValue, newValue) -> oldValue,
                LinkedHashMap::new
            ));
    }
    
    /**
     * Gets all animal type objects
     */
    public List<AnimalType> getAllAnimalTypeObjects() {
        return animalTypes.stream()
            .sorted(Comparator.comparing(AnimalType::getOrder))
            .collect(Collectors.toList());
    }
    
    /**
     * Validates if animal type code exists
     */
    public boolean isValidAnimalType(String code) {
        return findByCode(code) != null;
    }
    
    /**
     * Gets all supported language codes
     */
    public Set<String> getSupportedLanguages() {
        Set<String> languages = new HashSet<>();
        for (AnimalType animalType : animalTypes) {
            languages.addAll(animalType.getAllDisplayNames().keySet());
        }
        return languages;
    }
    
    /**
     * Find animal type by code
     */
    private AnimalType findByCode(String code) {
        return animalTypes.stream()
            .filter(at -> at.getCode().equals(code))
            .findFirst()
            .orElse(null);
    }
}
