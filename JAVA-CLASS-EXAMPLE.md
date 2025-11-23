# Exemplo de Java Class no Camunda BPMN

Este documento explica como usar `camunda:class` no BPMN para executar tarefas Java.

## 🎯 O que é camunda:class?

`camunda:class` é um atributo BPMN que permite especificar uma classe Java que será executada quando a Service Task for acionada.

## 📝 Sintaxe no BPMN

```xml
<bpmn:serviceTask id="Activity_CalculateDifference" 
                  name="Calculate High-Low Difference" 
                  camunda:class="com.example.camunda.embedded.javaclass.CalculateDifferenceTask">
```

## 🔧 Implementação da Classe

### Requisitos

A classe DEVE:
1. Implementar `org.camunda.bpm.engine.delegate.JavaDelegate`
2. Ter um método `execute(DelegateExecution execution)`
3. Ser anotada com `@Component` (para Spring Boot)

### Exemplo Completo

```java
package com.example.camunda.embedded.javaclass;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CalculateDifferenceTask implements JavaDelegate {

    private final CurrencyService currencyService;

    @Autowired
    public CalculateDifferenceTask(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        // 1. Buscar dados
        CurrencyResponse data = currencyService.getFullCurrencyData();
        
        // 2. Extrair valores
        Double high = Double.parseDouble(data.getUsdBrl().getHigh());
        Double low = Double.parseDouble(data.getUsdBrl().getLow());
        
        // 3. Calcular diferença
        Double difference = high - low;
        Double percentage = ((high - low) / low) * 100;
        
        // 4. Armazenar no processo
        execution.setVariable("currencyHigh", high);
        execution.setVariable("currencyLow", low);
        execution.setVariable("currencyDifference", difference);
        execution.setVariable("currencyPercentageVariation", percentage);
    }
}
```

## 🎨 Fluxo Completo no Projeto

```
Start Event
    ↓
Get Currency (JavaDelegate)
    ↓ [busca cotação da API]
Process Quote (Spring Expression)
    ↓ [formata dados]
Calculate Difference (Java Class) ← NOVO!
    ↓ [calcula high - low]
Validate Quote (Spring Expression)
    ↓ [valida cotação]
Review Currency (User Task)
    ↓
End Event
```

## 📊 Variáveis Criadas

A task `CalculateDifferenceTask` cria as seguintes variáveis:

| Variável | Tipo | Descrição | Exemplo |
|----------|------|-----------|---------|
| `currencyHigh` | Double | Valor mais alto do dia | 5.1234 |
| `currencyLow` | Double | Valor mais baixo do dia | 5.0123 |
| `currencyDifference` | Double | Diferença absoluta | 0.1111 |
| `currencyPercentageVariation` | Double | Variação percentual | 2.22 |
| `currencySpread` | Double | Amplitude (spread) | 0.1111 |
| `formattedHigh` | String | High formatado | "5,1234" |
| `formattedLow` | String | Low formatado | "5,0123" |
| `formattedDifference` | String | Diferença formatada | "0,1111" |
| `formattedPercentage` | String | Percentual formatado | "2.22%" |
| `significantVariation` | Boolean | Se variação > 1% | true |
| `calculationSuccess` | Boolean | Se cálculo foi bem-sucedido | true |

## 🧪 Testando

### 1. Execute a aplicação

```bash
cd poc-camunda-spring
run-embedded.bat  # ou ./run-embedded.sh
```

### 2. Inicie um processo

```bash
curl -X POST http://localhost:8080/api/process/start
```

### 3. Consulte as variáveis

```bash
curl http://localhost:8080/api/process/SEU_PROCESS_ID/variables
```

**Resposta esperada:**
```json
{
  "success": true,
  "processInstanceId": "12345",
  "variables": {
    "usdBrlHigh": 5.1234,
    "currencyHigh": 5.1234,
    "currencyLow": 5.0123,
    "currencyDifference": 0.1111,
    "currencyPercentageVariation": 2.22,
    "formattedDifference": "0,1111",
    "formattedPercentage": "2.22%",
    "significantVariation": true,
    "calculationSuccess": true
  }
}
```

## 🔍 Diferenças entre as Implementações

### 1. camunda:class

```xml
<bpmn:serviceTask camunda:class="com.example.MyTask">
```

**Características:**
- ✅ Especifica classe completa (package + nome)
- ✅ Com Spring Boot, usa o bean do contexto
- ✅ Permite injeção de dependência (@Autowired)
- ✅ Implementa JavaDelegate
- ⚠️ Precisa ser @Component

### 2. camunda:delegateExpression

```xml
<bpmn:serviceTask camunda:delegateExpression="${myTask}">
```

**Características:**
- ✅ Usa nome do bean Spring
- ✅ Mais comum e recomendado
- ✅ Permite injeção de dependência
- ✅ Implementa JavaDelegate

### 3. camunda:expression

```xml
<bpmn:serviceTask camunda:expression="${myService.method(execution)}">
```

**Características:**
- ✅ Chama método diretamente
- ✅ Não precisa implementar JavaDelegate
- ✅ Mais flexível
- ✅ Classe Spring comum

## 📋 Comparação Completa

| Aspecto | camunda:class | camunda:delegateExpression | camunda:expression |
|---------|---------------|----------------------------|-------------------|
| **Sintaxe** | Nome completo da classe | Nome do bean | Expressão com método |
| **Exemplo** | `com.example.MyTask` | `${myTask}` | `${service.method()}` |
| **Interface** | JavaDelegate | JavaDelegate | Qualquer |
| **Spring Bean** | Sim (@Component) | Sim | Sim |
| **Injeção** | ✅ Sim | ✅ Sim | ✅ Sim |
| **Flexibilidade** | ⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| **Uso no Projeto** | CalculateDifferenceTask | GetCurrencyDelegate | CurrencyProcessingService |

## 💡 Quando Usar Cada Um?

### Use camunda:class quando:
- Quer especificar a classe completa no BPMN
- Está migrando de Camunda sem Spring
- Quer deixar explícito qual classe é usada

### Use camunda:delegateExpression quando:
- Quer usar nome do bean (mais limpo)
- Está seguindo padrões do Camunda
- Quer flexibilidade para trocar implementação

### Use camunda:expression quando:
- Quer chamar método específico
- Não quer implementar JavaDelegate
- Tem lógica simples em serviço Spring

## 🎓 Exemplo Prático

### Cenário: Calcular diferença entre High e Low

**Dados da API:**
```json
{
  "USDBRL": {
    "high": "5.1234",
    "low": "5.0123"
  }
}
```

**Cálculos:**
- Diferença: 5.1234 - 5.0123 = 0.1111
- Percentual: (0.1111 / 5.0123) × 100 = 2.22%

**Resultado no Processo:**
```
currencyHigh: 5.1234
currencyLow: 5.0123
currencyDifference: 0.1111
currencyPercentageVariation: 2.22
formattedDifference: "0,1111"
formattedPercentage: "2.22%"
significantVariation: true (porque > 1%)
```

## 🔧 Troubleshooting

### Erro: "Cannot instantiate class"

**Causa**: Classe não é um Spring Bean

**Solução**: Adicione `@Component`
```java
@Component  // ← Adicione isso
public class MyTask implements JavaDelegate {
```

### Erro: "No bean found"

**Causa**: Classe não está no package escaneado

**Solução**: Verifique `@ComponentScan` ou mova para package correto

### Erro: "Cannot inject dependencies"

**Causa**: Construtor sem @Autowired

**Solução**: Adicione @Autowired no construtor
```java
@Autowired  // ← Adicione isso
public MyTask(MyService service) {
```

## 📚 Recursos Adicionais

- [Camunda Delegation Code](https://docs.camunda.org/manual/7.20/user-guide/process-engine/delegation-code/)
- [Spring Boot Integration](https://docs.camunda.org/manual/7.20/user-guide/spring-boot-integration/)
- [JavaDelegate API](https://docs.camunda.org/javadoc/camunda-bpm-platform/7.20/org/camunda/bpm/engine/delegate/JavaDelegate.html)
