package com.example.camunda.worker;

import com.example.camunda.service.CurrencyService;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.annotation.Variable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Worker do Camunda responsável por executar a tarefa de obter cotação.
 * 
 * Este worker é acionado quando o processo BPMN chega na Service Task
 * com o tipo "get-currency". Ele consome a API de cotação e retorna
 * o valor "high" como variável do processo.
 * 
 * Anotações:
 * - @JobWorker: Define que este método processa jobs do tipo especificado
 * - @Variable: Injeta variáveis do processo no método
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GetCurrencyWorker {

    private final CurrencyService currencyService;

    /**
     * Processa jobs do tipo "get-currency".
     * 
     * Este método é chamado automaticamente pelo Camunda quando uma
     * Service Task do tipo "get-currency" precisa ser executada.
     * 
     * Fluxo:
     * 1. Recebe o job do Camunda
     * 2. Chama o serviço para obter a cotação
     * 3. Retorna o valor "high" como variável do processo
     * 
     * Tratamento de erros:
     * - Se a API retornar erro (status != 200), retorna 0.0
     * - Se houver timeout, retorna 0.0
     * - Se houver erro de parsing, retorna 0.0
     * 
     * @param job Job ativado pelo Camunda
     * @return Map com variáveis a serem adicionadas ao processo
     */
    @JobWorker(type = "get-currency", autoComplete = true)
    public Map<String, Object> getCurrency(final ActivatedJob job) {
        log.info("=================================================");
        log.info("Processando job: {}", job.getKey());
        log.info("Tipo: {}", job.getType());
        log.info("Process Instance: {}", job.getProcessInstanceKey());
        log.info("=================================================");

        // Obtém o valor "high" da cotação USD-BRL
        Double usdBrlHigh = currencyService.getUsdBrlHigh();
        
        log.info("Valor obtido da API: {}", usdBrlHigh);

        // Prepara variáveis para retornar ao processo
        Map<String, Object> variables = new HashMap<>();
        variables.put("usdBrlHigh", usdBrlHigh);
        variables.put("currencyFetchSuccess", usdBrlHigh > 0.0);
        variables.put("fetchTimestamp", System.currentTimeMillis());

        log.info("Variáveis retornadas ao processo: {}", variables);
        log.info("Job {} concluído com sucesso", job.getKey());
        
        return variables;
    }

    /**
     * Worker alternativo que aceita parâmetros do processo.
     * 
     * Este método demonstra como receber variáveis do processo
     * e usá-las na lógica do worker.
     * 
     * @param job Job ativado
     * @param currencyPair Par de moedas (opcional, padrão USD-BRL)
     * @return Map com variáveis do processo
     */
    @JobWorker(type = "get-currency-with-params", autoComplete = true)
    public Map<String, Object> getCurrencyWithParams(
            final ActivatedJob job,
            @Variable(name = "currencyPair") String currencyPair) {
        
        log.info("Processando job com parâmetros: {}", job.getKey());
        log.info("Par de moedas solicitado: {}", currencyPair);

        // Por enquanto, sempre retorna USD-BRL
        // Pode ser expandido para suportar outros pares
        Double usdBrlHigh = currencyService.getUsdBrlHigh();
        
        Map<String, Object> variables = new HashMap<>();
        variables.put("usdBrlHigh", usdBrlHigh);
        variables.put("requestedPair", currencyPair);
        variables.put("currencyFetchSuccess", usdBrlHigh > 0.0);
        
        return variables;
    }
}
