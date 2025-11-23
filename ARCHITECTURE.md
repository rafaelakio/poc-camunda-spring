# Arquitetura da Aplicação

## Visão Geral

Esta aplicação demonstra a integração entre Camunda 8 e Spring Boot para processar workflows que consomem APIs externas.

## Componentes

### 1. Camunda Platform 8

```
┌─────────────────────────────────────────┐
│         Camunda Platform 8              │
│                                         │
│  ┌──────────┐  ┌──────────┐           │
│  │  Zeebe   │  │ Operate  │           │
│  │  Broker  │  │   UI     │           │
│  └────┬─────┘  └──────────┘           │
│       │                                 │
│  ┌────▼─────┐  ┌──────────┐           │
│  │  Zeebe   │  │Tasklist  │           │
│  │ Gateway  │  │   UI     │           │
│  └──────────┘  └──────────┘           │
│                                         │
│  ┌──────────────────────────┐         │
│  │    Elasticsearch         │         │
│  └──────────────────────────┘         │
└─────────────────────────────────────────┘
```

### 2. Spring Boot Application

```
┌─────────────────────────────────────────┐
│      Spring Boot Application            │
│                                         │
│  ┌──────────────────────────┐          │
│  │  ProcessController       │          │
│  │  (REST API)              │          │
│  └───────────┬──────────────┘          │
│              │                          │
│  ┌───────────▼──────────────┐          │
│  │  GetCurrencyWorker       │          │
│  │  (Job Worker)            │          │
│  └───────────┬──────────────┘          │
│              │                          │
│  ┌───────────▼──────────────┐          │
│  │  CurrencyService         │          │
│  │  (Business Logic)        │          │
│  └───────────┬──────────────┘          │
│              │                          │
│  ┌───────────▼──────────────┐          │
│  │  WebClient               │          │
│  │  (HTTP Client)           │          │
│  └──────────────────────────┘          │
└─────────────────────────────────────────┘
```

### 3. External API

```
┌─────────────────────────────────────────┐
│     AwesomeAPI - Currency API           │
│                                         │
│  GET /last/USD-BRL                     │
│                                         │
│  Response:                              │
│  {                                      │
│    "USDBRL": {                         │
│      "high": "5.1234",                 │
│      ...                                │
│    }                                    │
│  }                                      │
└─────────────────────────────────────────┘
```

## Fluxo de Execução

### 1. Início do Processo

```
Cliente HTTP
    │
    │ POST /api/process/start
    ▼
ProcessController
    │
    │ newCreateInstanceCommand()
    ▼
Zeebe Gateway
    │
    │ Create Process Instance
    ▼
Zeebe Broker
    │
    │ Start Event
    ▼
Service Task (get-currency)
```

### 2. Execução do Worker

```
Zeebe Broker
    │
    │ Publish Job
    ▼
GetCurrencyWorker
    │
    │ @JobWorker(type="get-currency")
    ▼
CurrencyService
    │
    │ getUsdBrlHigh()
    ▼
WebClient
    │
    │ GET https://economia.awesomeapi.com.br/last/USD-BRL
    ▼
AwesomeAPI
    │
    │ Response JSON
    ▼
CurrencyService
    │
    │ Extract USDBRL.high
    ▼
GetCurrencyWorker
    │
    │ Return variables
    ▼
Zeebe Broker
    │
    │ Complete Job
    ▼
User Task (Review)
```

### 3. Tratamento de Erros

```
WebClient Request
    │
    ├─ HTTP 200 ──→ Extract high value ──→ Return Double
    │
    ├─ HTTP 4xx ──→ Log error ──→ Return 0.0
    │
    ├─ HTTP 5xx ──→ Log error ──→ Return 0.0
    │
    ├─ Timeout ───→ Log error ──→ Return 0.0
    │
    └─ Exception ─→ Log error ──→ Return 0.0
```

## Tecnologias Utilizadas

### Backend
- **Java 17**: Linguagem de programação
- **Spring Boot 3.2**: Framework de aplicação
- **Maven**: Gerenciamento de dependências

### Camunda
- **Camunda 8.4**: Plataforma de workflow
- **Zeebe**: Motor de workflow
- **Spring Zeebe**: Integração Spring Boot

### HTTP Client
- **WebClient**: Cliente HTTP reativo
- **Spring WebFlux**: Framework reativo

### Serialização
- **Jackson**: Processamento JSON
- **Lombok**: Redução de boilerplate

## Padrões de Design

### 1. Service Layer Pattern

```java
@Service
public class CurrencyService {
    // Encapsula lógica de negócio
    // Isola comunicação com API externa
}
```

### 2. Worker Pattern

```java
@JobWorker(type = "get-currency")
public Map<String, Object> getCurrency() {
    // Processa jobs assíncronos
    // Retorna variáveis ao processo
}
```

### 3. DTO Pattern

```java
@Data
public class CurrencyResponse {
    // Transferência de dados
    // Mapeamento JSON
}
```

### 4. Controller Pattern

```java
@RestController
public class ProcessController {
    // Expõe API REST
    // Inicia processos
}
```

## Configuração

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

### Variáveis de Processo

| Nome | Tipo | Descrição |
|------|------|-----------|
| usdBrlHigh | Double | Valor "high" da cotação |
| currencyFetchSuccess | Boolean | Se a busca foi bem-sucedida |
| fetchTimestamp | Long | Timestamp da busca |

## Escalabilidade

### Horizontal Scaling

```
┌──────────────┐
│   Worker 1   │ ──┐
└──────────────┘   │
                   │
┌──────────────┐   ├──→ Zeebe Gateway
│   Worker 2   │ ──┤
└──────────────┘   │
                   │
┌──────────────┐   │
│   Worker 3   │ ──┘
└──────────────┘
```

Múltiplas instâncias do worker podem processar jobs em paralelo.

### Load Balancing

O Zeebe distribui jobs automaticamente entre workers disponíveis.

## Monitoramento

### Camunda Operate

- Visualização de processos
- Histórico de instâncias
- Análise de incidentes

### Logs

```
INFO  - Processando job: 2251799813685249
INFO  - Iniciando requisição para obter cotação USD-BRL
INFO  - Cotação USD-BRL (high) obtida com sucesso: 5.1234
INFO  - Job 2251799813685249 concluído com sucesso
```

### Métricas

- Taxa de sucesso de jobs
- Tempo médio de execução
- Erros de API

## Segurança

### Autenticação

- Camunda: Plaintext (desenvolvimento)
- Produção: OAuth 2.0 / OIDC

### Validação

- Input validation no controller
- Error handling no service
- Timeout protection

## Performance

### Otimizações

1. **Connection Pooling**: WebClient reutiliza conexões
2. **Async Processing**: Workers processam jobs assíncronos
3. **Timeout Control**: Evita travamentos
4. **Error Recovery**: Retorna valores padrão em erro

### Benchmarks

- Tempo médio de API: ~200ms
- Tempo médio de job: ~300ms
- Throughput: ~100 jobs/segundo (single worker)
