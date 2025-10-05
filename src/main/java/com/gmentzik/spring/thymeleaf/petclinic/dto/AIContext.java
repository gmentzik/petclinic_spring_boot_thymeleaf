package com.gmentzik.spring.thymeleaf.petclinic.dto;

public class AIContext {
    private String customerInfo;
    private String petInfo;

    // Getters and Setters
    public String getCustomerInfo() {
        return customerInfo;
    }

    public void setCustomerInfo(String customerInfo) {
        this.customerInfo = customerInfo;
    }

    public String getPetInfo() {
        return petInfo;
    }

    public void setPetInfo(String petInfo) {
        this.petInfo = petInfo;
    }
}
