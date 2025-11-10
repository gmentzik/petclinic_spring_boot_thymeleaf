package com.gmentzik.spring.thymeleaf.petclinic;

import com.gmentzik.spring.thymeleaf.petclinic.config.FileStorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(FileStorageProperties.class)
public class PetClinicApplication {

    

	public static void main(String[] args) {
		SpringApplication.run(PetClinicApplication.class, args);
	}

}
