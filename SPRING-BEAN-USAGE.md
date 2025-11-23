# Usando Spring Beans no Camunda BPMN

Este documento explica as diferentes formas de integrar classes Spring com processos BPMN no Camunda.

## 📋 Três Formas de Integração

### 1. JavaDelegate (Interface Camunda)

**Quando usar**: Quando você precisa de controle total sobre a execução e quer usar a interface padrão do Camunda.

**Implementação:**
```java
@Component("getCurrencyDelegate")
public class GetCurrencyDelegate implements JavaDelegate {
    @Override
    public void execute(DelegateExecution execution) throws Exception {
        // Sua lógica aqui
    }
}
```

**No BPMN:**
```xml
<bpmn:serviceTask id="Task1" 
                  name="Get Currency" 
                  camunda:delegateExpression="${getCurrencyDelegate}">
```

**Características:**
- ✅ Interface padrão do Camunda
- ✅ Controle total sobre execução
- ✅ Pode lançar exceções que param o processo
- ❌ Acoplado ao Camunda

---

### 2. Spring Bean com Expression (Método Direto)

**Quando usar**: Quando você quer usar uma classe Spring comum sem implementar interfaces do Camunda.

**Implementação:**
```java
@Service
public class CurrencyProcessingService {
    public void processQuote(DelegateExecution execution) {
        // Sua lógica aqui
        Double value = (Double) execution.getVariable("usdBrlHigh");
        execution.setVariable("processed", true);
    }
}
```

**No BPMN:**
```xml
<bpmn:serviceTask id="Task2" 
                  name="Process Quote" 
                  camunda:expression="${currencyProcessingService.processQuote(execution)}">
```

**Características:**
- ✅ Classe Spring comum
- ✅ Não precisa implementar interface
- ✅ Pode ter múltiplos métodos
- ✅ Testável independentemente
- ⚠️ Precisa receber DelegateExecution como parâmetro

---

### 3. Spring Bean com Class (Instanciação Direta)

**Quando usar**: Quando você quer que o Camunda instancie a classe diretamente (menos comum).

**Implementação:**
```java
public class MyProcessor implements JavaDelegate {
    @Override
    public void execute(DelegateExecution execution) throws Exception {
        // Sua lógica aqui
    }
}
```

**No BPMN:**
```xml
<bpmn:serviceTask id="Task3" 
                  name="Process" 
                  camunda:class="com.example.MyProcessor">
```

**Características:**
- ⚠️ Camunda cria nova instância a cada execução
- ⚠️ Não pode usar @Autowired (sem injeção de dependência)
- ❌ Menos flexível
- ❌ Não recomendado para Spring Boot

---

## 🎯 Exemplo Prático no Projeto

### Fluxo Implementado

```
Start
  ↓
Get Currency (JavaDelegate)
  ↓
Process Quote (Spring Bean Expression)
  ↓
Validate Quote (Spring Bean Expression)
  ↓
Review (User Task)
  ↓
End
```

### 1. Get Currency - JavaDelegate

```java
@Component("getCurrencyDelegate")
public class GetCurrencyDelegate implements JavaDelegate {
    private final CurrencyService currencyService;
    
    @Override
    public void execute(DelegateExecution execution) {
        Double value = currencyService.getUsdBrlHigh();
        execution.setVariable("usdBrlHigh", value);
    }
}
```

**BPMN:**
```xml
<bpmn:serviceTask id="Activity_GetCurrency" 
                  camunda:delegateExpression="${getCurrencyDelegate}">
```

### 2. Process Quote - Spring Bean

```java
@Service
public class CurrencyProcessingService {
    public void processQuote(DelegateExecution execution) {
        Double value = (Double) execution.getVariable("usdBrlHigh");
        String formatted = String.format("R$ %.4f", value);
        execution.setVariable("formattedQuote", formatted);
    }
}
```

**BPMN:**
```xml
<bpmn:serviceTask id="Activity_ProcessQuote" 
                  camunda:expression="${currencyProcessingService.processQuote(execution)}">
```

### 3. Validate Quote - Spring Bean

```java
@Service
public class CurrencyProcessingService {
    public void validateQuote(DelegateExecution execution) {
        Double value = (Double) execution.getVariable("usdBrlHigh");
        boolean valid = value > 0.0 && value < 10.0;
        execution.setVariable("validationResult", valid ? "VALID" : "INVALID");
    }
}
```

**BPMN:**
```xml
<bpmn:serviceTask id="Activity_ValidateQuote" 
                  camunda:expression="${currencyProcessingService.validateQuote(execution)}">
```

---

## 🔄 Comparação Detalhada

| Aspecto | JavaDelegate | Spring Expression | Spring Class |
|---------|--------------|-------------------|--------------|
| **Sintaxe BPMN** | `delegateExpression="${bean}"` | `expression="${bean.method()}"` | `class="com.example.Class"` |
| **Injeção de Dependência** | ✅ Sim | ✅ Sim | ❌ Não |
| **Testabilidade** | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐ |
| **Flexibilidade** | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐ |
| **Performance** | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐ |
| **Recomendado** | ✅ Sim | ✅ Sim | ❌ Não |

---

## 💡 Boas Práticas

### 1. Use Spring Expression para Lógica Simples

```java
@Service
public class NotificationService {
    public void sendEmail(DelegateExecution execution) {
        String email = (String) execution.getVariable("userEmail");
        // Envia email
    }
}
```

```xml
<bpmn:serviceTask camunda:expression="${notificationService.sendEmail(execution)}">
```

### 2. Use JavaDelegate para Lógica Complexa

```java
@Component("complexProcessor")
public class ComplexProcessor implements JavaDelegate {
    @Autowired
    private MultipleServices services;
    
    @Override
    public void execute(DelegateExecution execution) {
        // Lógica complexa com múltiplas dependências
    }
}
```

### 3. Separe Responsabilidades

```java
// ✅ BOM: Serviço focado
@Service
public class CurrencyFormatter {
    public String format(Double value) {
        return String.format("R$ %.4f", value);
    }
}

// ❌ RUIM: Tudo em um lugar
@Component("doEverything")
public class DoEverything implements JavaDelegate {
    public void execute(DelegateExecution execution) {
        // Faz tudo...
    }
}
```

### 4. Trate Erros Adequadamente

```java
@Service
public class SafeProcessor {
    public void process(DelegateExecution execution) {
        try {
            // Lógica de negócio
        } catch (Exception e) {
            log.error("Erro: {}", e.getMessage());
            execution.setVariable("error", e.getMessage());
            execution.setVariable("success", false);
            // Não propaga exceção se quiser continuar o processo
        }
    }
}
```

---

## 🧪 Testando

### Teste de JavaDelegate

```java
@Test
void testDelegate() {
    DelegateExecution execution = mock(DelegateExecution.class);
    GetCurrencyDelegate delegate = new GetCurrencyDelegate(currencyService);
    
    delegate.execute(execution);
    
    verify(execution).setVariable(eq("usdBrlHigh"), any(Double.class));
}
```

### Teste de Spring Bean

```java
@Test
void testSpringBean() {
    DelegateExecution execution = mock(DelegateExecution.class);
    when(execution.getVariable("usdBrlHigh")).thenReturn(5.0);
    
    CurrencyProcessingService service = new CurrencyProcessingService();
    service.processQuote(execution);
    
    verify(execution).setVariable(eq("formattedQuote"), anyString());
}
```

---

## 📚 Recursos Adicionais

- [Camunda Delegation Code](https://docs.camunda.org/manual/7.20/user-guide/process-engine/delegation-code/)
- [Spring Boot Integration](https://docs.camunda.org/manual/7.20/user-guide/spring-boot-integration/)
- [Expression Language](https://docs.camunda.org/manual/7.20/user-guide/process-engine/expression-language/)

---

## 🎓 Resumo

**Use JavaDelegate quando:**
- Precisa de controle total
- Quer usar a interface padrão do Camunda
- Tem lógica complexa com múltiplas dependências

**Use Spring Expression quando:**
- Quer código mais limpo e testável
- Tem lógica simples de negócio
- Quer reutilizar serviços Spring existentes
- Quer múltiplos métodos na mesma classe

**Evite Spring Class quando:**
- Estiver usando Spring Boot (sempre!)
- Precisar de injeção de dependência
- Quiser código testável
