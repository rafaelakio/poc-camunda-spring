package com.example.camunda.service;

import com.example.camunda.dto.CurrencyResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Serviço responsável por consumir a API de cotação de moedas.
 * 
 * Este serviço encapsula a lógica de comunicação com a API externa,
 * tratamento de erros HTTP e extração do valor "high" da cotação USD-BRL.
 */
@Slf4j
@Service
public class CurrencyService {

    private final WebClient webClient;
    private final int timeout;

    /**
     * Construtor que inicializa o WebClient com a URL base da API.
     *
     * @param apiUrl URL da API de cotação
     * @param timeout Timeout em milissegundos para requisições
     */
    public CurrencyService(
            @Value("${currency.api.url}") String apiUrl,
            @Value("${currency.api.timeout}") int timeout) {
        this.webClient = WebClient.builder()
                .baseUrl(apiUrl)
                .build();
        this.timeout = timeout;
        log.info("CurrencyService inicializado com URL: {} e timeout: {}ms", apiUrl, timeout);
    }

    /**
     * Obtém o valor "high" da cotação USD-BRL.
     * 
     * Faz uma requisição GET para a API, trata os códigos HTTP de resposta
     * e extrai o valor "high" do objeto USDBRL.
     * 
     * Tratamento de erros:
     * - HTTP 200: Retorna o valor "high" convertido para Double
     * - HTTP 4xx/5xx: Retorna 0.0
     * - Timeout: Retorna 0.0
     * - Erro de parsing: Retorna 0.0
     * 
     * @return Valor "high" da cotação ou 0.0 em caso de erro
     */
    public Double getUsdBrlHigh() {
        log.info("Iniciando requisição para obter cotação USD-BRL");
        
        try {
            CurrencyResponse response = webClient.get()
                    .retrieve()
                    // Trata status codes diferentes de 2xx
                    .onStatus(
                            HttpStatus::is4xxClientError,
                            clientResponse -> {
                                log.error("Erro 4xx ao consultar API: {}", clientResponse.statusCode());
                                return Mono.error(new RuntimeException("Client error: " + clientResponse.statusCode()));
                            }
                    )
                    .onStatus(
                            HttpStatus::is5xxServerError,
                            clientResponse -> {
                                log.error("Erro 5xx ao consultar API: {}", clientResponse.statusCode());
                                return Mono.error(new RuntimeException("Server error: " + clientResponse.statusCode()));
                            }
                    )
                    .bodyToMono(CurrencyResponse.class)
                    .timeout(Duration.ofMillis(timeout))
                    .block();

            // Valida se a resposta contém os dados esperados
            if (response == null || response.getUsdBrl() == null) {
                log.warn("Resposta da API não contém dados USDBRL");
                return 0.0;
            }

            String highValue = response.getUsdBrl().getHigh();
            
            if (highValue == null || highValue.isEmpty()) {
                log.warn("Valor 'high' não encontrado na resposta");
                return 0.0;
            }

            // Converte string para Double
            Double result = Double.parseDouble(highValue);
            log.info("Cotação USD-BRL (high) obtida com sucesso: {}", result);
            
            return result;

        } catch (Exception e) {
            // Qualquer erro (timeout, parsing, network) retorna 0.0
            log.error("Erro ao obter cotação USD-BRL: {}", e.getMessage(), e);
            return 0.0;
        }
    }

    /**
     * Obtém informações completas da cotação USD-BRL.
     * 
     * Método auxiliar que retorna o objeto completo da resposta,
     * útil para debugging e logs detalhados.
     * 
     * @return Objeto CurrencyResponse completo ou null em caso de erro
     */
    public CurrencyResponse getFullCurrencyData() {
        log.info("Obtendo dados completos da cotação USD-BRL");
        
        try {
            return webClient.get()
                    .retrieve()
                    .bodyToMono(CurrencyResponse.class)
                    .timeout(Duration.ofMillis(timeout))
                    .block();
        } catch (Exception e) {
            log.error("Erro ao obter dados completos da cotação: {}", e.getMessage());
            return null;
        }
    }
}
