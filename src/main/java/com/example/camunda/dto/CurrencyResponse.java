package com.example.camunda.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO para resposta da API de cotação de moedas.
 * 
 * Representa a estrutura JSON retornada pela API:
 * {
 *   "USDBRL": {
 *     "code": "USD",
 *     "codein": "BRL",
 *     "name": "Dólar Americano/Real Brasileiro",
 *     "high": "5.1234",
 *     "low": "5.0123",
 *     "varBid": "0.0123",
 *     "pctChange": "0.24",
 *     "bid": "5.1000",
 *     "ask": "5.1100",
 *     "timestamp": "1234567890",
 *     "create_date": "2024-01-15 10:30:00"
 *   }
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyResponse {
    
    /**
     * Objeto USDBRL contendo informações da cotação.
     */
    @JsonProperty("USDBRL")
    private UsdBrlData usdBrl;
    
    /**
     * Classe interna representando os dados da cotação USD-BRL.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UsdBrlData {
        
        /**
         * Código da moeda de origem (USD).
         */
        private String code;
        
        /**
         * Código da moeda de destino (BRL).
         */
        private String codein;
        
        /**
         * Nome descritivo da cotação.
         */
        private String name;
        
        /**
         * Valor mais alto do dia.
         * Este é o campo que será extraído pelo processo.
         */
        private String high;
        
        /**
         * Valor mais baixo do dia.
         */
        private String low;
        
        /**
         * Variação do valor de compra.
         */
        private String varBid;
        
        /**
         * Percentual de mudança.
         */
        private String pctChange;
        
        /**
         * Valor de compra.
         */
        private String bid;
        
        /**
         * Valor de venda.
         */
        private String ask;
        
        /**
         * Timestamp da cotação.
         */
        private String timestamp;
        
        /**
         * Data de criação formatada.
         */
        @JsonProperty("create_date")
        private String createDate;
    }
}
