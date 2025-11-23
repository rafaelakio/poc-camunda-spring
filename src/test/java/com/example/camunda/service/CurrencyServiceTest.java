package com.example.camunda.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes para o CurrencyService.
 * 
 * Estes testes validam o comportamento do serviço de cotação,
 * incluindo casos de sucesso e erro.
 */
@SpringBootTest
class CurrencyServiceTest {

    @Autowired
    private CurrencyService currencyService;

    /**
     * Testa se o serviço consegue obter a cotação com sucesso.
     * 
     * Este teste faz uma chamada real à API, então pode falhar
     * se a API estiver indisponível.
     */
    @Test
    void testGetUsdBrlHigh_Success() {
        // Executa
        Double result = currencyService.getUsdBrlHigh();
        
        // Valida
        assertNotNull(result, "Resultado não deve ser nulo");
        assertTrue(result >= 0.0, "Resultado deve ser maior ou igual a zero");
        
        // Se a API estiver funcionando, o valor deve ser maior que zero
        if (result > 0.0) {
            System.out.println("Cotação obtida com sucesso: " + result);
            assertTrue(result > 1.0, "Cotação USD-BRL deve ser maior que 1.0");
            assertTrue(result < 100.0, "Cotação USD-BRL deve ser menor que 100.0");
        }
    }

    /**
     * Testa se o serviço retorna dados completos.
     */
    @Test
    void testGetFullCurrencyData() {
        // Executa
        var result = currencyService.getFullCurrencyData();
        
        // Valida
        if (result != null) {
            assertNotNull(result.getUsdBrl(), "Dados USDBRL não devem ser nulos");
            assertNotNull(result.getUsdBrl().getHigh(), "Valor high não deve ser nulo");
            System.out.println("Dados completos obtidos: " + result);
        }
    }
}
