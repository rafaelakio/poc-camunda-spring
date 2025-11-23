package com.example.camunda;

import io.camunda.zeebe.spring.client.annotation.Deployment;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Aplicação principal do Camunda Spring Boot.
 * 
 * Esta aplicação demonstra o uso do Camunda 8 com Spring Boot
 * para processar workflows BPMN que consomem APIs externas.
 */
@SpringBootApplication
@Deployment(resources = "classpath*:bpmn/*.bpmn")
public class CamundaApplication {

    public static void main(String[] args) {
        SpringApplication.run(CamundaApplication.class, args);
    }
}
