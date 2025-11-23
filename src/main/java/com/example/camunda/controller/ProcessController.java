package com.example.camunda.controller;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller REST para iniciar e gerenciar processos Camunda.
 * 
 * Fornece endpoints para:
 * - Iniciar instâncias de processo
 * - Consultar status
 * - Testar a aplicação
 */
@Slf4j
@RestController
@RequestMapping("/api/process")
@RequiredArgsConstructor
public class ProcessController {

    private final ZeebeClient zeebeClient;

    /**
     * Inicia uma nova instância do processo de cotação.
     * 
     * Endpoint: POST /api/process/start
     * 
     * @return Informações da instância criada
     */
    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> startProcess() {
        log.info("Recebida requisição para iniciar processo de cotação");

        try {
            // Inicia o processo sem variáveis iniciais
            ProcessInstanceEvent event = zeebeClient.newCreateInstanceCommand()
                    .bpmnProcessId("currency-process")
                    .latestVersion()
                    .send()
                    .join();

            log.info("Processo iniciado com sucesso. Instance Key: {}", event.getProcessInstanceKey());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("processInstanceKey", event.getProcessInstanceKey());
            response.put("bpmnProcessId", event.getBpmnProcessId());
            response.put("version", event.getVersion());
            response.put("message", "Processo iniciado com sucesso");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erro ao iniciar processo: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Inicia processo com variáveis customizadas.
     * 
     * Endpoint: POST /api/process/start-with-vars
     * 
     * @param variables Variáveis iniciais do processo
     * @return Informações da instância criada
     */
    @PostMapping("/start-with-vars")
    public ResponseEntity<Map<String, Object>> startProcessWithVariables(
            @RequestBody Map<String, Object> variables) {
        
        log.info("Iniciando processo com variáveis: {}", variables);

        try {
            ProcessInstanceEvent event = zeebeClient.newCreateInstanceCommand()
                    .bpmnProcessId("currency-process")
                    .latestVersion()
                    .variables(variables)
                    .send()
                    .join();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("processInstanceKey", event.getProcessInstanceKey());
            response.put("bpmnProcessId", event.getBpmnProcessId());
            response.put("version", event.getVersion());
            response.put("variables", variables);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erro ao iniciar processo: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Endpoint de health check.
     * 
     * Endpoint: GET /api/process/health
     * 
     * @return Status da aplicação
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("application", "poc-camunda-spring");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }
}
