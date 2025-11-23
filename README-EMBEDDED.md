# POC Camunda Spring - Versão Embedded (SEM DOCKER)

Esta é a versão **embedded** da aplicação que **NÃO requer Docker**. Usa Camunda 7 com banco H2 in-memory.

## 🎯 Diferenças entre as Versões

| Característica | Camunda 8 (Docker) | Camunda 7 Embedded |
|----------------|--------------------|--------------------|
| Requer Docker | ✅ Sim | ❌ Não |
| Banco de Dados | Elasticsearch | H2 in-memory |
| Worker | @JobWorker | JavaDelegate |
| UI Incluída | Operate, Tasklist | Cockpit, Tasklist |
| Complexidade | Maior | Menor |
| Produção | Recomendado | Desenvolvimento |

## 🚀 Início Rápido (SEM DOCKER)

### 1. Compile com o POM Embedded

```bash
cd poc-camunda-spring
mvn clean install -f pom-embedded.xml
```

### 2. Execute a Aplicação

```bash
mvn spring-boot:run -f pom-embedded.xml -Dspring-boot.run.profiles=embedded
```

Ou execute diretamente a classe:

```bash
java -jar target/poc-camunda-spring-embedded-1.0.0.jar --spring.profiles.active=embedded
```

### 3. Acesse as Interfaces

- **Aplicação REST**: http://localhost:8080
- **Camunda Cockpit**: http://localhost:8080/camunda/app/cockpit
- **Camunda Tasklist**: http://localhost:8080/camunda/app/tasklist
- **H2 Console**: http://localhost:8080/h2-console

**Login**: demo / demo

## 📖 Como Usar

### Iniciar um Processo

```bash
curl -X POST http://localhost:8080/api/process/start
```

**Resposta:**
```json
{
  "success": true,
  "processInstanceId": "12345",
  "processDefinitionId": "currency-process-embedded:1:67890",
  "message": "Processo iniciado com sucesso"
}
```

### Consultar Variáveis do Processo

```bash
curl http://localhost:8080/api/process/12345/variables
```

**Resposta:**
```json
{
  "success": true,
  "processInstanceId": "12345",
  "variables": {
    "usdBrlHigh": 5.1234,
    "currencyFetchSuccess": true,
    "fetchTimestamp": 1705329600000
  }
}
```

### Health Check

```bash
curl http://localhost:8080/api/process/health
```

## 🔧 Componentes Principais

### GetCurrencyDelegate

Java Delegate que implementa a lógica de negócio:

```java
@Component("getCurrencyDelegate")
public class GetCurrencyDelegate implements JavaDelegate {
    
    @Override
    public void execute(DelegateExecution execution) {
        // Obtém cotação da API
        Double usdBrlHigh = currencyService.getUsdBrlHigh();
        
        // Armazena no processo
        execution.setVariable("usdBrlHigh", usdBrlHigh);
        execution.setVariable("currencyFetchSuccess", usdBrlHigh > 0.0);
    }
}
```

### BPMN Process

O processo usa `camunda:delegateExpression` para chamar o delegate:

```xml
<bpmn:serviceTask id="Activity_GetCurrency" 
                  name="Get USD-BRL Currency" 
                  camunda:delegateExpression="${getCurrencyDelegate}">
```

## 📊 Visualizar no Cockpit

1. Acesse: http://localhost:8080/camunda/app/cockpit
2. Login: demo / demo
3. Clique em "Processes"
4. Selecione "Currency Process Embedded"
5. Veja as instâncias em execução

## ✅ Vantagens da Versão Embedded

- ✅ **Sem Docker**: Roda direto no Java
- ✅ **Rápido**: Inicia em segundos
- ✅ **Simples**: Menos componentes
- ✅ **Desenvolvimento**: Ideal para testes locais
- ✅ **Debug**: Fácil de debugar no IDE

## ⚠️ Limitações

- ❌ Banco in-memory (dados perdidos ao reiniciar)
- ❌ Não recomendado para produção
- ❌ Menos escalável que Camunda 8
- ❌ UI menos moderna

## 🔄 Migração entre Versões

### De Embedded para Camunda 8

1. Use o `pom.xml` original
2. Substitua `JavaDelegate` por `@JobWorker`
3. Ajuste o BPMN para usar `zeebe:taskDefinition`
4. Inicie o Docker Compose

### De Camunda 8 para Embedded

1. Use o `pom-embedded.xml`
2. Substitua `@JobWorker` por `JavaDelegate`
3. Ajuste o BPMN para usar `camunda:delegateExpression`
4. Execute sem Docker

## 📝 Configuração

### application-embedded.yml

```yaml
camunda:
  bpm:
    admin-user:
      id: demo
      password: demo
    database:
      schema-update: true
    auto-deployment-enabled: true

spring:
  datasource:
    url: jdbc:h2:mem:camunda
    driver-class-name: org.h2.Driver
```

## 🧪 Testando

### Teste Completo

```bash
# 1. Inicie a aplicação
mvn spring-boot:run -f pom-embedded.xml -Dspring-boot.run.profiles=embedded

# 2. Inicie um processo
curl -X POST http://localhost:8080/api/process/start

# 3. Veja os logs
# Você verá: "Cotação USD-BRL (high) obtida com sucesso: 5.1234"

# 4. Acesse o Cockpit
# http://localhost:8080/camunda/app/cockpit (demo/demo)
```

## 🎓 Quando Usar Cada Versão

### Use Camunda 7 Embedded quando:
- Desenvolvimento local
- Testes rápidos
- Prototipagem
- Aprendizado
- Sem acesso ao Docker

### Use Camunda 8 quando:
- Produção
- Alta escalabilidade
- Microserviços
- Cloud-native
- Recursos modernos

## 📚 Recursos Adicionais

- [Documentação Camunda 7](https://docs.camunda.org/manual/7.20/)
- [Spring Boot Camunda](https://docs.camunda.org/manual/7.20/user-guide/spring-boot-integration/)
- [Java Delegates](https://docs.camunda.org/manual/7.20/user-guide/process-engine/delegation-code/)

## 🆘 Troubleshooting

### Porta 8080 em uso

```bash
# Mude a porta em application-embedded.yml
server:
  port: 8081
```

### Banco H2 não conecta

```bash
# Acesse: http://localhost:8080/h2-console
# JDBC URL: jdbc:h2:mem:camunda
# User: sa
# Password: (vazio)
```

### Processo não inicia

- Verifique se o BPMN está em `src/main/resources/bpmn/`
- Confirme que `auto-deployment-enabled: true`
- Veja os logs de startup

## 🎉 Pronto!

Agora você pode testar a aplicação Camunda **sem Docker**! 🚀
