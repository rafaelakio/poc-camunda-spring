package com.example.camunda.embedded.javaclass;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

/**
 * Exemplo de Java Class SEM Spring (sem @Component).
 * 
 * Esta classe demonstra como seria usar camunda:class
 * sem integração com Spring Boot.
 * 
 * LIMITAÇÕES:
 * - ❌ Não pode usar @Autowired
 * - ❌ Não pode injetar dependências
 * - ❌ Precisa criar instâncias manualmente
 * - ❌ Nova instância criada a cada execução
 * - ❌ Não pode usar outros beans Spring
 * 
 * QUANDO USAR:
 * - Camunda standalone (sem Spring)
 * - Lógica muito simples sem dependências
 * - Não precisa de serviços externos
 */
@Slf4j
// NÃO tem @Component - Camunda cria instância diretamente
public class CalculateDifferenceWithoutSpring implements JavaDelegate {

    /**
     * Construtor padrão (sem parâmetros).
     * 
     * OBRIGATÓRIO quando não usa Spring.
     * Camunda usa reflexão para criar instância.
     */
    public CalculateDifferenceWithoutSpring() {
        log.info("Construtor chamado - Nova instância criada pelo Camunda");
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        log.info("=================================================");
        log.info("Executando SEM Spring (@Component)");
        log.info("Process Instance ID: {}", execution.getProcessInstanceId());
        log.info("=================================================");

        try {
            // ❌ NÃO PODE FAZER ISSO (sem @Autowired):
            // private final CurrencyService currencyService;
            
            // ✅ PRECISA FAZER ASSIM:
            // Criar instâncias manualmente ou usar valores fixos
            
            // Exemplo: Lê variáveis do processo
            Double high = (Double) execution.getVariable("currencyHigh");
            Double low = (Double) execution.getVariable("currencyLow");

            if (high != null && low != null) {
                // Cálculo simples sem dependências
                Double difference = high - low;
                Double percentage = ((high - low) / low) * 100;

                execution.setVariable("simpleDifference", difference);
                execution.setVariable("simplePercentage", percentage);

                log.info("Cálculo realizado:");
                log.info("  - High: {}", high);
                log.info("  - Low: {}", low);
                log.info("  - Diferença: {}", difference);
                log.info("  - Percentual: {}%", percentage);
            } else {
                log.warn("Variáveis high/low não encontradas");
            }

        } catch (Exception e) {
            log.error("Erro: {}", e.getMessage(), e);
            throw e; // Propaga erro para Camunda
        }

        log.info("Execução concluída (sem Spring)");
    }
}
