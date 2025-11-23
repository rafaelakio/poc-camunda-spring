# Guia de Contribuição - POC Camunda Spring

Obrigado por considerar contribuir com este projeto! Este documento fornece diretrizes para colaboração.

## 📋 Índice

- [Código de Conduta](#código-de-conduta)
- [Como Contribuir](#como-contribuir)
- [Estrutura do Projeto](#estrutura-do-projeto)
- [Desenvolvimento Local](#desenvolvimento-local)
- [Padrões de Código](#padrões-de-código)
- [Processo de Pull Request](#processo-de-pull-request)
- [Testando](#testando)

## 🤝 Código de Conduta

Este projeto adere a um código de conduta. Ao participar, você concorda em manter um ambiente respeitoso e colaborativo.

## 🚀 Como Contribuir

### 1. Fork e Clone

```bash
# Fork o repositório no GitHub
# Clone seu fork
git clone https://github.com/seu-usuario/poc-camunda-spring.git
cd poc-camunda-spring
```

### 2. Configure o Ambiente

**Opção 1: Camunda 7 Embedded (Recomendado para desenvolvimento)**

```bash
# Windows
run-embedded.bat

# Linux/Mac
chmod +x run-embedded.sh
./run-embedded.sh
```

**Opção 2: Camunda 8 com Docker**

```bash
# Inicie o Camunda 8
docker-compose up -d

# Compile e execute
mvn clean install
mvn spring-boot:run
```

### 3. Crie uma Branch

```bash
git checkout -b feature/minha-contribuicao
```

### 4. Faça suas Alterações

- Modifique código Java
- Atualize processos BPMN
- Adicione testes
- Atualize documentação

### 5. Teste suas Alterações

```bash
# Compile
mvn clean install

# Execute testes
mvn test

# Execute a aplicação
mvn spring-boot:run
```

### 6. Commit e Push

```bash
git add .
git commit -m "feat: adiciona nova funcionalidade"
git push origin feature/minha-contribuicao
```

### 7. Abra um Pull Request

- Vá para o repositório original no GitHub
- Clique em "New Pull Request"
- Selecione sua branch
- Descreva suas alterações detalhadamente

## 📁 Estrutura do Projeto

```
poc-camunda-spring/
├── src/
│   ├── main/
│   │   ├── java/com/example/camunda/
│   │   │   ├── CamundaApplication.java      # Classe principal
│   │   │   ├── controller/                  # Controllers REST
│   │   │   │   └── ProcessController.java
│   │   │   ├── dto/                         # Data Transfer Objects
│   │   │   │   └── CurrencyResponse.java
│   │   │   ├── service/                     # Serviços de negócio
│   │   │   │   └── CurrencyService.java
│   │   │   └── worker/                      # Workers Camunda
│   │   │       └── GetCurrencyWorker.java
│   │   └── resources/
│   │       ├── application.yml              # Configuração
│   │       └── bpmn/                        # Processos BPMN
│   │           └── currency-process.bpmn
│   └── test/                                # Testes
├── pom.xml                                  # Dependências Maven
├── pom-embedded.xml                         # POM para Camunda 7
├── docker-compose.yml                       # Camunda 8 Docker
└── README.md                                # Documentação
```

## 💻 Desenvolvimento Local

### Pré-requisitos

- Java 17 ou superior
- Maven 3.6+
- Docker (opcional, para Camunda 8)
- IDE (IntelliJ IDEA, Eclipse, VS Code)

### Configuração da IDE

#### IntelliJ IDEA

1. Abra o projeto (File → Open → selecione pasta)
2. Aguarde o Maven importar dependências
3. Configure JDK 17 (File → Project Structure → Project SDK)
4. Execute `CamundaApplication.java`

#### VS Code

1. Instale extensões:
   - Extension Pack for Java
   - Spring Boot Extension Pack
2. Abra a pasta do projeto
3. Execute via Spring Boot Dashboard

### Executando Localmente

```bash
# Compilar
mvn clean install

# Executar
mvn spring-boot:run

# Ou com profile específico
mvn spring-boot:run -Dspring-boot.run.profiles=embedded
```

## 📝 Padrões de Código

### Java

#### Nomenclatura

```java
// Classes: PascalCase
public class CurrencyService { }

// Interfaces: PascalCase (sem prefixo I)
public interface ProcessService { }

// Métodos: camelCase
public Double getUsdBrlHigh() { }

// Variáveis: camelCase
private String apiUrl;

// Constantes: UPPER_SNAKE_CASE
public static final String WORKFLOW_NAME = "currency-process";

// Packages: lowercase
package com.example.camunda.service;
```

#### Anotações Spring

```java
// Service
@Service
public class CurrencyService {
    private final RestTemplate restTemplate;
    
    @Autowired
    public CurrencyService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
}

// Controller
@RestController
@RequestMapping("/api/process")
public class ProcessController {
    
    @PostMapping("/start")
    public ResponseEntity<?> startProcess(@RequestBody Map<String, Object> input) {
        // Implementação
    }
}

// Worker Camunda
@Component
public class MyWorker {
    
    @JobWorker(type = "my-task", autoComplete = true)
    public Map<String, Object> handleTask(final ActivatedJob job) {
        // Implementação
    }
}
```

#### Tratamento de Erros

```java
// ✓ Correto: Trate exceções específicas
try {
    Double value = currencyService.getUsdBrlHigh();
} catch (RestClientException ex) {
    logger.error("Erro ao chamar API: {}", ex.getMessage());
    return 0.0;
} catch (Exception ex) {
    logger.error("Erro inesperado", ex);
    throw new RuntimeException("Falha ao obter cotação", ex);
}

// ✗ Evite: Catch genérico sem logging
try {
    // código
} catch (Exception e) {
    // silencioso
}
```

#### Logging

```java
// ✓ Correto: Use SLF4J com placeholders
logger.info("Processando job: {} para processo: {}", jobKey, processInstanceKey);

// ✗ Evite: Concatenação de strings
logger.info("Processando job: " + jobKey + " para processo: " + processInstanceKey);
```

### BPMN

#### Nomenclatura de Elementos

- **Process ID**: kebab-case (`currency-process`)
- **Task ID**: camelCase (`getCurrency`)
- **Task Name**: Title Case (`Get Currency`)
- **Task Type**: kebab-case (`get-currency`)

#### Boas Práticas BPMN

```xml
<!-- Service Task -->
<bpmn:serviceTask id="getCurrency" name="Get Currency">
  <bpmn:extensionElements>
    <zeebe:taskDefinition type="get-currency" />
  </bpmn:extensionElements>
</bpmn:serviceTask>

<!-- User Task -->
<bpmn:userTask id="reviewValue" name="Review Currency Value">
  <bpmn:extensionElements>
    <zeebe:formDefinition formKey="review-form" />
  </bpmn:extensionElements>
</bpmn:userTask>
```

## 🔄 Processo de Pull Request

### Checklist

Antes de submeter um PR, verifique:

- [ ] Código compila sem erros (`mvn clean install`)
- [ ] Testes passam (`mvn test`)
- [ ] Código segue os padrões do projeto
- [ ] Documentação foi atualizada
- [ ] BPMN está válido (se aplicável)
- [ ] Commit messages são descritivas
- [ ] Branch está atualizada com main

### Formato de Commit Messages

Use o padrão Conventional Commits:

```
tipo(escopo): descrição curta

Descrição mais detalhada se necessário.

Fixes #123
```

**Tipos:**
- `feat`: Nova funcionalidade
- `fix`: Correção de bug
- `docs`: Alterações na documentação
- `style`: Formatação de código
- `refactor`: Refatoração
- `test`: Adição ou correção de testes
- `chore`: Tarefas de manutenção

**Exemplos:**

```bash
feat(worker): adiciona worker para validação de dados

Implementa novo worker que valida CPF/CNPJ antes de processar.
Inclui testes unitários e atualização do BPMN.

Closes #45
```

```bash
fix(service): corrige timeout em chamadas de API

Aumenta timeout de 10s para 30s para APIs lentas.
Adiciona retry com backoff exponencial.

Fixes #67
```

## 🧪 Testando

### Testes Unitários

```java
@SpringBootTest
class CurrencyServiceTest {
    
    @Autowired
    private CurrencyService currencyService;
    
    @Test
    void testGetUsdBrlHigh() {
        Double value = currencyService.getUsdBrlHigh();
        assertNotNull(value);
        assertTrue(value > 0);
    }
}
```

### Testes de Integração

```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class ProcessControllerIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void testStartProcess() {
        Map<String, Object> input = Map.of("test", "value");
        
        ResponseEntity<Map> response = restTemplate.postForEntity(
            "/api/process/start",
            input,
            Map.class
        );
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
```

### Executar Testes

```bash
# Todos os testes
mvn test

# Testes específicos
mvn test -Dtest=CurrencyServiceTest

# Com cobertura
mvn test jacoco:report
```

## 🐛 Reportando Bugs

### Template de Bug Report

```markdown
**Descrição do Bug**
Descrição clara e concisa do problema.

**Como Reproduzir**
1. Execute '...'
2. Configure '...'
3. Observe '...'

**Comportamento Esperado**
O que deveria acontecer.

**Comportamento Atual**
O que está acontecendo.

**Logs**
```
Cole logs relevantes aqui
```

**Ambiente:**
- Java version: [17, 21]
- Maven version: [3.8, 3.9]
- Camunda version: [7, 8]
- OS: [Windows 10, Ubuntu 20.04, etc]

**Contexto Adicional**
Qualquer outra informação relevante.
```

## 💡 Sugerindo Melhorias

### Template de Feature Request

```markdown
**Problema a Resolver**
Descrição clara do problema ou necessidade.

**Solução Proposta**
Como você imagina que isso deveria funcionar.

**Alternativas Consideradas**
Outras abordagens que você considerou.

**Contexto Adicional**
Screenshots, exemplos, referências, etc.
```

## 🎯 Áreas para Contribuição

### Funcionalidades Desejadas

- [ ] Autenticação e autorização
- [ ] Testes unitários e de integração
- [ ] Métricas e monitoramento
- [ ] Suporte a múltiplos processos
- [ ] Interface web para visualização
- [ ] Documentação de API (Swagger/OpenAPI)
- [ ] CI/CD pipeline
- [ ] Containerização com Docker

### Melhorias de Código

- [ ] Tratamento de erros mais robusto
- [ ] Logging estruturado
- [ ] Validação de entrada
- [ ] Cache de resultados
- [ ] Retry policies configuráveis
- [ ] Health checks

### Documentação

- [ ] Tutoriais passo a passo
- [ ] Exemplos de uso avançado
- [ ] Diagramas de arquitetura
- [ ] Vídeos explicativos
- [ ] FAQ expandido

## 📚 Recursos Úteis

- [Documentação Camunda 8](https://docs.camunda.io/)
- [Documentação Camunda 7](https://docs.camunda.org/)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [BPMN 2.0 Specification](https://www.omg.org/spec/BPMN/2.0/)
- [Conventional Commits](https://www.conventionalcommits.org/)

## ❓ Dúvidas

Se tiver dúvidas sobre como contribuir:

1. Abra uma issue com a tag `question`
2. Descreva sua dúvida claramente
3. Aguarde resposta da comunidade

## 🙏 Agradecimentos

Obrigado por contribuir! Sua ajuda torna este projeto melhor para todos.

---

**Nota**: Este é um projeto educacional. Sinta-se livre para experimentar e aprender!
