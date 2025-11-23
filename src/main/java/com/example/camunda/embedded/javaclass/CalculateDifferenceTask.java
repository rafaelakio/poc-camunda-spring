package com.example.camunda.embedded.javaclass;

import com.example.camunda.dto.CurrencyResponse;
import com.example.camunda.service.CurrencyService;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;

/**
 * Java Class Task para calcular a diferença entre High e Low.
 * 
 * Esta classe demonstra o uso de camunda:class no BPMN.
 * 
 * IMPORTANTE: Para usar camunda:class com Spring Boot, a classe DEVE:
 * 1. Ser anotada com @Component (para Spring gerenciar)
 * 2. Implementar JavaDelegate
 * 3. Ter construtor padrão ou @Autowired
 * 
 * Diferença entre camunda:class e camunda:delegateExpression:
 * 
 * camunda:class="com.example.ClassName"
 * - Camunda tenta instanciar a classe
 * - Com Spring Boot, usa o bean do contexto
 * - Permite injeção de dependência
 * 
 * camunda:delegateExpression="${beanName}"
 * - Usa diretamente o bean do Spring
 * - Mais comum e recomendado
 * 
 * Esta implementação busca os dados completos da API e calcula:
 * - Diferença absoluta entre high e low
 * - Percentual de variação
 * - Amplitude do dia
 */
@Slf4j
@Component
public class CalculateDifferenceTask implements JavaDelegate {

    private final CurrencyService currencyService;

    /**
     * Construtor com injeção de dependência.
     * 
     * @param currencyService Serviço para buscar dados da API
     */
    @Autowired
    public CalculateDifferenceTask(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    /**
     * Executa o cálculo da diferença entre high e low.
     * 
     * Este método:
     * 1. Busca dados completos da API
     * 2. Extrai valores high e low
     * 3. Calcula diferença absoluta
     * 4. Calcula percentual de variação
     * 5. Armazena resultados no processo
     * 
     * @param execution Contexto de execução do processo
     * @throws Exception Se houver erro no processamento
     */
    @Override
    public void execute(DelegateExecution execution) throws Exception {
        log.info("=================================================");
        log.info("Executando CalculateDifferenceTask (Java Class)");
        log.info("Process Instance ID: {}", execution.getProcessInstanceId());
        log.info("Activity ID: {}", execution.getCurrentActivityId());
        log.info("=================================================");

        try {
            // Busca dados completos da API
            log.info("Buscando dados completos da API...");
            CurrencyResponse fullData = currencyService.getFullCurrencyData();

            if (fullData == null || fullData.getUsdBrl() == null) {
                log.warn("Dados da API não disponíveis");
                setDefaultValues(execution);
                return;
            }

            CurrencyResponse.UsdBrlData usdBrlData = fullData.getUsdBrl();

            // Extrai valores high e low
            String highStr = usdBrlData.getHigh();
            String lowStr = usdBrlData.getLow();

            log.info("Valores obtidos da API:");
            log.info("  - High: {}", highStr);
            log.info("  - Low: {}", lowStr);

            if (highStr == null || lowStr == null || 
                highStr.isEmpty() || lowStr.isEmpty()) {
                log.warn("Valores high ou low não disponíveis");
                setDefaultValues(execution);
                return;
            }

            // Converte para Double
            Double high = Double.parseDouble(highStr);
            Double low = Double.parseDouble(lowStr);

            // Calcula diferença absoluta
            Double difference = high - low;

            // Calcula percentual de variação em relação ao low
            Double percentageVariation = ((high - low) / low) * 100;

            // Calcula amplitude (spread)
            Double spread = difference;

            // Formata valores para exibição
            DecimalFormat df = new DecimalFormat("#,##0.0000");
            String formattedHigh = df.format(high);
            String formattedLow = df.format(low);
            String formattedDifference = df.format(difference);
            String formattedPercentage = String.format("%.2f%%", percentageVariation);

            // Armazena variáveis no processo
            execution.setVariable("currencyHigh", high);
            execution.setVariable("currencyLow", low);
            execution.setVariable("currencyDifference", difference);
            execution.setVariable("currencyPercentageVariation", percentageVariation);
            execution.setVariable("currencySpread", spread);
            
            // Variáveis formatadas para exibição
            execution.setVariable("formattedHigh", formattedHigh);
            execution.setVariable("formattedLow", formattedLow);
            execution.setVariable("formattedDifference", formattedDifference);
            execution.setVariable("formattedPercentage", formattedPercentage);
            
            // Dados adicionais da API
            execution.setVariable("currencyBid", usdBrlData.getBid());
            execution.setVariable("currencyAsk", usdBrlData.getAsk());
            execution.setVariable("currencyName", usdBrlData.getName());
            execution.setVariable("currencyTimestamp", usdBrlData.getTimestamp());
            
            execution.setVariable("calculationSuccess", true);

            // Log dos resultados
            log.info("Cálculos realizados com sucesso:");
            log.info("  - High: R$ {}", formattedHigh);
            log.info("  - Low: R$ {}", formattedLow);
            log.info("  - Diferença: R$ {}", formattedDifference);
            log.info("  - Variação: {}", formattedPercentage);
            log.info("  - Spread: R$ {}", df.format(spread));

            // Determina se a variação é significativa (> 1%)
            boolean significantVariation = percentageVariation > 1.0;
            execution.setVariable("significantVariation", significantVariation);
            
            if (significantVariation) {
                log.warn("ATENÇÃO: Variação significativa detectada (> 1%)");
            }

        } catch (NumberFormatException e) {
            log.error("Erro ao converter valores numéricos: {}", e.getMessage());
            setDefaultValues(execution);
            execution.setVariable("calculationError", "Erro ao converter valores: " + e.getMessage());
            
        } catch (Exception e) {
            log.error("Erro ao calcular diferença: {}", e.getMessage(), e);
            setDefaultValues(execution);
            execution.setVariable("calculationError", e.getMessage());
        }

        log.info("CalculateDifferenceTask concluído");
    }

    /**
     * Define valores padrão em caso de erro.
     * 
     * @param execution Contexto de execução
     */
    private void setDefaultValues(DelegateExecution execution) {
        execution.setVariable("currencyHigh", 0.0);
        execution.setVariable("currencyLow", 0.0);
        execution.setVariable("currencyDifference", 0.0);
        execution.setVariable("currencyPercentageVariation", 0.0);
        execution.setVariable("currencySpread", 0.0);
        execution.setVariable("calculationSuccess", false);
        execution.setVariable("significantVariation", false);
        
        log.warn("Valores padrão definidos devido a erro");
    }
}
