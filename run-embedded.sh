#!/bin/bash
# Script para executar a versão embedded (SEM DOCKER)

echo "=========================================="
echo "  Camunda Spring - Versão Embedded"
echo "  (SEM DOCKER - Camunda 7)"
echo "=========================================="
echo ""

echo "1. Compilando aplicação..."
mvn clean install -f pom-embedded.xml

if [ $? -ne 0 ]; then
    echo ""
    echo "ERRO: Falha na compilação!"
    exit 1
fi

echo ""
echo "2. Iniciando aplicação..."
echo ""
echo "Aguarde alguns segundos..."
echo ""
echo "Acessos:"
echo "- Aplicação: http://localhost:8080"
echo "- Cockpit: http://localhost:8080/camunda/app/cockpit"
echo "- Tasklist: http://localhost:8080/camunda/app/tasklist"
echo "- H2 Console: http://localhost:8080/h2-console"
echo ""
echo "Login: demo / demo"
echo ""
echo "Pressione Ctrl+C para parar"
echo ""

mvn spring-boot:run -f pom-embedded.xml -Dspring-boot.run.profiles=embedded
