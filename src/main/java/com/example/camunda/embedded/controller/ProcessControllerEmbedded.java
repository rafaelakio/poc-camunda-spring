package com.example.camunda.embedded.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller REST para iniciar e gerenciar processos Camunda 7.
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
public class ProcessControllerEmbedded {

    private final RuntimeService runtimeService;

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
            ProcessInstance processInstance = runtimeService
                    .startProcessInstanceByKey("currency-process-embedded");

            log.info("Processo iniciado com sucesso. Instance ID: {}", 
                    processInstance.getProcessInstanceId());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("processInstanceId", processInstance.getProcessInstanceId());
            response.put("processDefinitionId", processInstance.getProcessDefinitionId());
            response.put("businessKey", processInstance.getBusinessKey());
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
            ProcessInstance processInstance = runtimeService
                    .startProcessInstanceByKey("currency-process-embedded", variables);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("processInstanceId", processInstance.getProcessInstanceId());
            response.put("processDefinitionId", processInstance.getProcessDefinitionId());
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
     * Consulta variáveis de uma instância de processo.
     * 
     * Endpoint: GET /api/process/{processInstanceId}/variables
     * 
     * @param processInstanceId ID da instância do processo
     * @return Variáveis do processo
     */
    @GetMapping("/{processInstanceId}/variables")
    public ResponseEntity<Map<String, Object>> getProcessVariables(
            @PathVariable String processInstanceId) {
        
        log.info("Consultando variáveis do processo: {}", processInstanceId);

        try {
            Map<String, Object> variables = runtimeService
                    .getVariables(processInstanceId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("processInstanceId", processInstanceId);
            response.put("variables", variables);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erro ao consultar variáveis: {}", e.getMessage(), e);
            
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
        response.put("application", "poc-camunda-spring-embedded");
        response.put("camundaVersion", "7.20.0");
        response.put("mode", "embedded");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }
}
