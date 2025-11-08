package com.gmentzik.spring.thymeleaf.petclinic.repository;

import javax.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gmentzik.spring.thymeleaf.petclinic.entity.Customer;

@Repository
@Transactional
public interface CustomerRepository extends JpaRepository<Customer, Integer> {
  Page<Customer> findByFirstNameContainingIgnoreCaseOrSurNameContainingIgnoreCase(String firstName, String surName, Pageable pageable);
}
