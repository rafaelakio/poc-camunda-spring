# POC Camunda Spring - Currency API Integration

Aplicação de exemplo usando Camunda 8 (versão mais recente) com Spring Boot para processar workflows BPMN que consomem APIs externas.

## 📋 Visão Geral

Esta aplicação demonstra:
- Integração do Camunda 8 com Spring Boot
- Service Task com Worker Java
- Consumo de API REST externa (cotação USD-BRL)
- Tratamento de códigos HTTP
- Extração de dados JSON
- Tratamento de erros e timeouts

## 🎯 Funcionalidades

### Processo BPMN

O processo implementado realiza:

1. **Start Event**: Inicia o processo
2. **Service Task**: Consome API de cotação
   - Faz requisição GET para `https://economia.awesomeapi.com.br/last/USD-BRL`
   - Extrai o valor `USDBRL.high` do JSON
   - Trata códigos HTTP (retorna 0 se != 200)
3. **User Task**: Revisão do valor obtido
4. **End Event**: Finaliza o processo

### API REST

Endpoints disponíveis:

- `POST /api/process/start` - Inicia processo
- `POST /api/process/start-with-vars` - Inicia com variáveis
- `GET /api/process/health` - Health check

## 🚀 Pré-requisitos

### Versão com Docker (Camunda 8)
- Java 17 ou superior
- Maven 3.6+
- Docker e Docker Compose

### Versão SEM Docker (Camunda 7 Embedded)
- Java 17 ou superior
- Maven 3.6+
- **Nenhuma outra dependência!**

## 📦 Instalação

### Opção 1: SEM Docker (Camunda 7 Embedded) - RECOMENDADO PARA TESTES

**Mais rápido e simples!**

```bash
cd poc-camunda-spring

# Windows
run-embedded.bat

# Linux/Mac
chmod +x run-embedded.sh
./run-embedded.sh
```

Ou manualmente:

```bash
mvn clean install -f pom-embedded.xml
mvn spring-boot:run -f pom-embedded.xml -Dspring-boot.run.profiles=embedded
```

**Acessos:**
- Aplicação: http://localhost:8080
- Cockpit: http://localhost:8080/camunda/app/cockpit
- Tasklist: http://localhost:8080/camunda/app/tasklist
- Login: demo / demo

📖 **Documentação completa**: [README-EMBEDDED.md](README-EMBEDDED.md)

---

### Opção 2: COM Docker (Camunda 8)

```bash
cd poc-camunda-spring

# 1. Inicie o Camunda 8
docker-compose up -d

# 2. Aguarde ~2 minutos

# 3. Compile e execute
mvn clean install
mvn spring-boot:run
```

A aplicação estará disponível em: `http://localhost:8080`

## 🎮 Como Usar

### Iniciar um Processo

```bash
curl -X POST http://localhost:8080/api/process/start
```

**Resposta:**
```json
{
  "success": true,
  "processInstanceKey": 2251799813685249,
  "bpmnProcessId": "currency-process",
  "version": 1,
  "message": "Processo iniciado com sucesso"
}
```

### Verificar Health

```bash
curl http://localhost:8080/api/process/health
```

## 📊 Estrutura do Projeto

```
poc-camunda-spring/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/camunda/
│   │   │       ├── CamundaApplication.java
│   │   │       ├── controller/
│   │   │       │   └── ProcessController.java
│   │   │       ├── dto/
│   │   │       │   └── CurrencyResponse.java
│   │   │       ├── service/
│   │   │       │   └── CurrencyService.java
│   │   │       └── worker/
│   │   │           └── GetCurrencyWorker.java
│   │   └── resources/
│   │       ├── application.yml
│   │       └── bpmn/
│   │           └── currency-process.bpmn
│   └── test/
├── pom.xml
├── docker-compose.yml
└── README.md
```

## 🔧 Componentes Principais

### CurrencyService

Serviço responsável por consumir a API de cotação:

```java
public Double getUsdBrlHigh() {
    // Faz requisição GET
    // Trata códigos HTTP
    // Extrai USDBRL.high
    // Retorna 0.0 em caso de erro
}
```

**Tratamento de erros:**
- HTTP 4xx → Retorna 0.0
- HTTP 5xx → Retorna 0.0
- Timeout → Retorna 0.0
- Parsing error → Retorna 0.0

### GetCurrencyWorker

Worker que processa a Service Task:

```java
@JobWorker(type = "get-currency", autoComplete = true)
public Map<String, Object> getCurrency(final ActivatedJob job) {
    Double usdBrlHigh = currencyService.getUsdBrlHigh();
    
    Map<String, Object> variables = new HashMap<>();
    variables.put("usdBrlHigh", usdBrlHigh);
    variables.put("currencyFetchSuccess", usdBrlHigh > 0.0);
    
    return variables;
}
```

### ProcessController

Controller REST para gerenciar processos:

- Inicia instâncias de processo
- Fornece health check
- Aceita variáveis customizadas

## 📝 Configuração

### application.yml

```yaml
zeebe:
  client:
    broker:
      gateway-address: 127.0.0.1:26500
    security:
      plaintext: true

currency:
  api:
    url: https://economia.awesomeapi.com.br/last/USD-BRL
    timeout: 10000
```

### Variáveis do Processo

O processo armazena as seguintes variáveis:

- `usdBrlHigh` (Double): Valor "high" da cotação
- `currencyFetchSuccess` (Boolean): Se a busca foi bem-sucedida
- `fetchTimestamp` (Long): Timestamp da busca

## 🧪 Testando

### Teste Manual

1. Inicie a aplicação
2. Faça uma requisição POST para iniciar o processo
3. Verifique os logs para ver o valor obtido
4. Acesse o Camunda Operate em `http://localhost:8081`

### Teste com curl

```bash
# Iniciar processo
curl -X POST http://localhost:8080/api/process/start

# Health check
curl http://localhost:8080/api/process/health
```

### Teste da API de Cotação

```bash
# Testar API diretamente
curl https://economia.awesomeapi.com.br/last/USD-BRL
```

**Resposta esperada:**
```json
{
  "USDBRL": {
    "code": "USD",
    "codein": "BRL",
    "name": "Dólar Americano/Real Brasileiro",
    "high": "5.1234",
    "low": "5.0123",
    ...
  }
}
```

## 🐳 Docker Compose

O arquivo `docker-compose.yml` inclui:

- Zeebe Broker
- Zeebe Gateway
- Camunda Operate (UI)
- Elasticsearch

**Portas:**
- 26500: Zeebe Gateway
- 8081: Camunda Operate
- 9200: Elasticsearch

## 📚 Recursos Adicionais

- [Documentação Camunda 8](https://docs.camunda.io/)
- [Spring Boot Camunda](https://github.com/camunda-community-hub/spring-zeebe)
- [BPMN 2.0](https://www.omg.org/spec/BPMN/2.0/)

## 🔍 Logs

A aplicação gera logs detalhados:

```
Processando job: 2251799813685249
Tipo: get-currency
Process Instance: 2251799813685248
Iniciando requisição para obter cotação USD-BRL
Cotação USD-BRL (high) obtida com sucesso: 5.1234
Valor obtido da API: 5.1234
Job 2251799813685249 concluído com sucesso
```

## 🛠️ Troubleshooting

### Camunda não conecta

```bash
# Verifique se o Docker está rodando
docker ps

# Reinicie os containers
docker-compose restart
```

### Worker não processa jobs

- Verifique se a aplicação está rodando
- Confirme que o BPMN foi deployado
- Verifique logs do Zeebe

### API de cotação falha

- Teste a API diretamente com curl
- Verifique conectividade de rede
- Aumente o timeout em `application.yml`

## 📄 Licença

Este projeto é fornecido como exemplo educacional.
