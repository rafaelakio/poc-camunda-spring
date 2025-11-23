# Guia de Início Rápido - Camunda Spring

## 3 Passos para Começar

### 1. Inicie o Camunda (2 minutos)

```bash
cd poc-camunda-spring
docker-compose up -d
```

Aguarde cerca de 2 minutos para todos os serviços iniciarem.

### 2. Compile e Execute a Aplicação (1 minuto)

```bash
mvn clean install
mvn spring-boot:run
```

### 3. Inicie um Processo (30 segundos)

```bash
curl -X POST http://localhost:8080/api/process/start
```

Pronto! O processo está rodando e consumindo a API de cotação.

## Verificar Resultados

### Ver Logs

Os logs mostrarão o valor obtido:

```
Cotação USD-BRL (high) obtida com sucesso: 5.1234
Valor obtido da API: 5.1234
```

### Acessar Camunda Operate

1. Abra: http://localhost:8081
2. Login: demo / demo
3. Veja o processo em execução

### Acessar Camunda Tasklist

1. Abra: http://localhost:8082
2. Login: demo / demo
3. Complete a tarefa "Review Currency Value"

## Comandos Úteis

```bash
# Ver logs do Docker
docker-compose logs -f

# Parar Camunda
docker-compose down

# Reiniciar
docker-compose restart

# Limpar tudo
docker-compose down -v
```

## Próximos Passos

- Leia o [README.md](README.md) completo
- Explore o BPMN em `src/main/resources/bpmn/`
- Customize o worker em `GetCurrencyWorker.java`
- Adicione mais Service Tasks ao processo
