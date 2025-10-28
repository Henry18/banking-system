package com.bank.personasclientes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
public class PersonasClientesApplication {
  public static void main(String[] args) { SpringApplication.run(PersonasClientesApplication.class, args); }
}
