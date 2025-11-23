package com.example.camunda.embedded.delegate;

import com.example.camunda.service.CurrencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

/**
 * Java Delegate para Camunda 7 que consome a API de cotação.
 * 
 * Este delegate é chamado quando o processo BPMN executa a Service Task.
 * Ele consome a API de cotação USD-BRL e armazena o resultado como
 * variável do processo.
 * 
 * Implementa JavaDelegate do Camunda 7 (diferente do Worker do Camunda 8).
 */
@Slf4j
@Component("getCurrencyDelegate")
@RequiredArgsConstructor
public class GetCurrencyDelegate implements JavaDelegate {

    private final CurrencyService currencyService;

    /**
     * Executa a lógica do delegate.
     * 
     * Este método é chamado automaticamente pelo Camunda quando a
     * Service Task é executada.
     * 
     * Fluxo:
     * 1. Obtém a cotação USD-BRL da API
     * 2. Extrai o valor "high"
     * 3. Armazena como variável do processo
     * 
     * Tratamento de erros:
     * - Se a API retornar erro (status != 200), armazena 0.0
     * - Se houver timeout, armazena 0.0
     * - Se houver erro de parsing, armazena 0.0
     * 
     * @param execution Contexto de execução do processo
     * @throws Exception Se houver erro crítico
     */
    @Override
    public void execute(DelegateExecution execution) throws Exception {
        log.info("=================================================");
        log.info("Executando GetCurrencyDelegate");
        log.info("Process Instance ID: {}", execution.getProcessInstanceId());
        log.info("Activity ID: {}", execution.getCurrentActivityId());
        log.info("=================================================");

        try {
            // Obtém o valor "high" da cotação USD-BRL
            Double usdBrlHigh = currencyService.getUsdBrlHigh();
            
            log.info("Valor obtido da API: {}", usdBrlHigh);

            // Armazena variáveis no processo
            execution.setVariable("usdBrlHigh", usdBrlHigh);
            execution.setVariable("currencyFetchSuccess", usdBrlHigh > 0.0);
            execution.setVariable("fetchTimestamp", System.currentTimeMillis());

            // Log das variáveis armazenadas
            log.info("Variáveis armazenadas no processo:");
            log.info("  - usdBrlHigh: {}", usdBrlHigh);
            log.info("  - currencyFetchSuccess: {}", usdBrlHigh > 0.0);
            log.info("  - fetchTimestamp: {}", System.currentTimeMillis());

            log.info("Delegate executado com sucesso");

        } catch (Exception e) {
            log.error("Erro ao executar delegate: {}", e.getMessage(), e);
            
            // Em caso de erro, armazena valores padrão
            execution.setVariable("usdBrlHigh", 0.0);
            execution.setVariable("currencyFetchSuccess", false);
            execution.setVariable("errorMessage", e.getMessage());
            
            // Não propaga a exceção para não falhar o processo
            log.warn("Erro tratado, processo continuará com valores padrão");
        }
    }
}
