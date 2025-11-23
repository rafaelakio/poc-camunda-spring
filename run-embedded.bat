@echo off
REM Script para executar a versão embedded (SEM DOCKER)

echo ==========================================
echo   Camunda Spring - Versao Embedded
echo   (SEM DOCKER - Camunda 7)
echo ==========================================
echo.

echo 1. Compilando aplicacao...
call mvn clean install -f pom-embedded.xml

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERRO: Falha na compilacao!
    pause
    exit /b 1
)

echo.
echo 2. Iniciando aplicacao...
echo.
echo Aguarde alguns segundos...
echo.
echo Acessos:
echo - Aplicacao: http://localhost:8080
echo - Cockpit: http://localhost:8080/camunda/app/cockpit
echo - Tasklist: http://localhost:8080/camunda/app/tasklist
echo - H2 Console: http://localhost:8080/h2-console
echo.
echo Login: demo / demo
echo.
echo Pressione Ctrl+C para parar
echo.

call mvn spring-boot:run -f pom-embedded.xml -Dspring-boot.run.profiles=embedded

pause
