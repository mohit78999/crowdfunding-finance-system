package com.crowdfunding;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Crowdfunding Finance Management System.
 * Spring Boot auto-configures JPA, MVC, Thymeleaf, and the embedded Tomcat server.
 *
 * Design Pattern: MVC - the entire application follows the Spring MVC pattern
 * where @Controller classes handle HTTP, @Service classes hold business logic,
 * and @Repository interfaces manage persistence.
 */
@SpringBootApplication
public class CrowdFundingApplication {

    public static void main(String[] args) {
        SpringApplication.run(CrowdFundingApplication.class, args);
    }
}
