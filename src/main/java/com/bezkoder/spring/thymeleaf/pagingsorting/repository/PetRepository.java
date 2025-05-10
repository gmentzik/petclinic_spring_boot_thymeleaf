package com.bezkoder.spring.thymeleaf.pagingsorting.repository;

import javax.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.bezkoder.spring.thymeleaf.pagingsorting.entity.Pet;
import com.bezkoder.spring.thymeleaf.pagingsorting.entity.Customer;

@Repository
@Transactional
public interface PetRepository extends JpaRepository<Pet, Integer> {

  // You can define custom query methods here if needed
  //Page<Author> findByTutorial(Tutorial tutorial, Pageable pageable);

  // If you're using a custom query, make sure it's like this
  Page<Pet> findByCustomer(@Param("customer") Customer customer, Pageable pageable);

}