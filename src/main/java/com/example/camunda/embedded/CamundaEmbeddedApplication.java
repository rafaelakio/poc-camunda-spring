package com.example.camunda.embedded;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Aplicação principal do Camunda 7 Embedded.
 * 
 * Esta versão roda o Camunda completamente embedded na aplicação Spring Boot,
 * sem necessidade de Docker ou serviços externos.
 * 
 * Características:
 * - Camunda 7 embedded
 * - Banco H2 in-memory
 * - Cockpit UI disponível
 * - Tasklist UI disponível
 * - REST API disponível
 */
@SpringBootApplication
@ComponentScan(basePackages = {
    "com.example.camunda.embedded",
    "com.example.camunda.service",
    "com.example.camunda.dto"
})
public class CamundaEmbeddedApplication {

    public static void main(String[] args) {
        SpringApplication.run(CamundaEmbeddedApplication.class, args);
    }
}
