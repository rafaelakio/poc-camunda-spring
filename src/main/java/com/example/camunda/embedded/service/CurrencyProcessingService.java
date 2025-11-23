package com.example.camunda.embedded.service;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Serviço Spring para processamento de dados de cotação.
 * 
 * Esta classe é chamada diretamente do BPMN usando camunda:class.
 * Demonstra como usar uma classe Spring comum (não JavaDelegate)
 * no processo Camunda.
 * 
 * Diferenças:
 * - JavaDelegate: Implementa interface específica do Camunda
 * - Spring Bean: Classe Spring comum com método público
 * 
 * No BPMN:
 * - JavaDelegate: camunda:delegateExpression="${beanName}"
 * - Spring Bean: camunda:class="com.example.package.ClassName"
 *                ou camunda:expression="${beanName.methodName(execution)}"
 */
@Slf4j
@Service
public class CurrencyProcessingService {

    /**
     * Processa e formata os dados da cotação obtida.
     * 
     * Este método é chamado diretamente do BPMN usando:
     * camunda:expression="${currencyProcessingService.processQuote(execution)}"
     * 
     * Funcionalidades:
     * 1. Lê o valor da cotação das variáveis do processo
     * 2. Formata o valor para exibição
     * 3. Calcula informações adicionais
     * 4. Armazena resultados processados no processo
     * 
     * @param execution Contexto de execução do processo
     */
    public void processQuote(DelegateExecution execution) {
        log.info("=================================================");
        log.info("Processando cotação com Spring Service");
        log.info("Process Instance ID: {}", execution.getProcessInstanceId());
        log.info("Activity ID: {}", execution.getCurrentActivityId());
        log.info("=================================================");

        try {
            // Obtém o valor da cotação das variáveis do processo
            Double usdBrlHigh = (Double) execution.getVariable("usdBrlHigh");
            Boolean fetchSuccess = (Boolean) execution.getVariable("currencyFetchSuccess");

            log.info("Valor recebido: {}", usdBrlHigh);
            log.info("Fetch success: {}", fetchSuccess);

            if (usdBrlHigh != null && usdBrlHigh > 0.0) {
                // Formata o valor para exibição
                DecimalFormat df = new DecimalFormat("#,##0.0000");
                String formattedValue = df.format(usdBrlHigh);

                // Calcula valor em reais para 100 dólares
                Double valueFor100Dollars = usdBrlHigh * 100;
                String formatted100Dollars = df.format(valueFor100Dollars);

                // Determina se o dólar está alto ou baixo (threshold: R$ 5.00)
                String priceLevel = usdBrlHigh > 5.0 ? "ALTO" : "NORMAL";

                // Gera timestamp formatado
                String processedAt = LocalDateTime.now()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

                // Armazena variáveis processadas no processo
                execution.setVariable("formattedQuote", formattedValue);
                execution.setVariable("valueFor100Dollars", valueFor100Dollars);
                execution.setVariable("formatted100Dollars", formatted100Dollars);
                execution.setVariable("priceLevel", priceLevel);
                execution.setVariable("processedAt", processedAt);
                execution.setVariable("processingSuccess", true);

                log.info("Cotação processada com sucesso:");
                log.info("  - Valor formatado: R$ {}", formattedValue);
                log.info("  - 100 dólares: R$ {}", formatted100Dollars);
                log.info("  - Nível de preço: {}", priceLevel);
                log.info("  - Processado em: {}", processedAt);

            } else {
                log.warn("Valor inválido ou zero, marcando processamento como falho");
                execution.setVariable("processingSuccess", false);
                execution.setVariable("processingError", "Valor de cotação inválido");
            }

        } catch (Exception e) {
            log.error("Erro ao processar cotação: {}", e.getMessage(), e);
            execution.setVariable("processingSuccess", false);
            execution.setVariable("processingError", e.getMessage());
        }
    }

    /**
     * Valida se a cotação está dentro de limites aceitáveis.
     * 
     * Método alternativo que demonstra validação de negócio.
     * Pode ser chamado com:
     * camunda:expression="${currencyProcessingService.validateQuote(execution)}"
     * 
     * @param execution Contexto de execução
     */
    public void validateQuote(DelegateExecution execution) {
        log.info("Validando cotação...");

        Double usdBrlHigh = (Double) execution.getVariable("usdBrlHigh");

        if (usdBrlHigh == null || usdBrlHigh <= 0.0) {
            execution.setVariable("validationResult", "INVALID");
            execution.setVariable("validationMessage", "Cotação inválida ou zero");
            log.warn("Validação falhou: cotação inválida");
            return;
        }

        // Define limites de validação
        double minAcceptable = 1.0;
        double maxAcceptable = 10.0;

        if (usdBrlHigh < minAcceptable || usdBrlHigh > maxAcceptable) {
            execution.setVariable("validationResult", "OUT_OF_RANGE");
            execution.setVariable("validationMessage", 
                String.format("Cotação fora do range esperado (%.2f - %.2f)", 
                    minAcceptable, maxAcceptable));
            log.warn("Validação falhou: cotação fora do range");
        } else {
            execution.setVariable("validationResult", "VALID");
            execution.setVariable("validationMessage", "Cotação válida");
            log.info("Validação bem-sucedida: cotação dentro do range esperado");
        }
    }

    /**
     * Gera um relatório resumido da cotação.
     * 
     * Demonstra como retornar valores diretamente para o processo.
     * Pode ser usado em expressões condicionais no BPMN.
     * 
     * @param execution Contexto de execução
     * @return String com relatório formatado
     */
    public String generateReport(DelegateExecution execution) {
        log.info("Gerando relatório da cotação...");

        Double usdBrlHigh = (Double) execution.getVariable("usdBrlHigh");
        String formattedQuote = (String) execution.getVariable("formattedQuote");
        String priceLevel = (String) execution.getVariable("priceLevel");
        String processedAt = (String) execution.getVariable("processedAt");

        StringBuilder report = new StringBuilder();
        report.append("=== RELATÓRIO DE COTAÇÃO USD-BRL ===\n");
        report.append(String.format("Valor: R$ %s\n", formattedQuote));
        report.append(String.format("Nível: %s\n", priceLevel));
        report.append(String.format("Processado em: %s\n", processedAt));
        report.append("====================================");

        String reportText = report.toString();
        execution.setVariable("quotationReport", reportText);

        log.info("Relatório gerado:\n{}", reportText);

        return reportText;
    }

    /**
     * Calcula a variação percentual em relação a um valor de referência.
     * 
     * Exemplo de método que pode ser usado em gateways condicionais.
     * 
     * @param currentValue Valor atual
     * @param referenceValue Valor de referência
     * @return Percentual de variação
     */
    public Double calculateVariation(Double currentValue, Double referenceValue) {
        if (referenceValue == null || referenceValue == 0.0) {
            return 0.0;
        }
        return ((currentValue - referenceValue) / referenceValue) * 100;
    }
}
