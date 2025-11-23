# Terraform - POC Camunda Spring

Configurações Terraform para deploy da aplicação Camunda Spring na AWS e Azure.

## 📁 Estrutura

```
terraform/
├── aws/                    # Configuração AWS
│   ├── main.tf
│   ├── variables.tf
│   ├── outputs.tf
│   └── terraform.tfvars.example
└── azure/                  # Configuração Azure
    ├── main.tf
    ├── variables.tf
    ├── outputs.tf
    └── terraform.tfvars.example
```

## 🚀 Deploy na AWS

### Recursos Criados

- VPC com subnets públicas
- ECS Fargate Cluster
- Application Load Balancer
- ECR Repository
- CloudWatch Logs
- IAM Roles

### Pré-requisitos

- Terraform >= 1.0
- AWS CLI configurado
- Credenciais AWS

### Deploy

```bash
cd terraform/aws

# Copiar e editar variáveis
cp terraform.tfvars.example terraform.tfvars
# Edite terraform.tfvars com seus valores

# Inicializar Terraform
terraform init

# Planejar mudanças
terraform plan

# Aplicar configuração
terraform apply
```

### Build e Push da Imagem Docker

```bash
# Obter URL do ECR
ECR_URL=$(terraform output -raw ecr_repository_url)

# Login no ECR
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin $ECR_URL

# Build da imagem
docker build -t poc-camunda-spring ..

# Tag da imagem
docker tag poc-camunda-spring:latest $ECR_URL:latest

# Push da imagem
docker push $ECR_URL:latest
```

### Acessar Aplicação

```bash
# Obter URL do Load Balancer
terraform output application_url
```

## 🔷 Deploy no Azure

### Recursos Criados

- Resource Group
- Azure Container Registry (ACR)
- App Service Plan
- App Service (Web App for Containers)
- Application Insights
- Log Analytics Workspace

### Pré-requisitos

- Terraform >= 1.0
- Azure CLI configurado
- Credenciais Azure

### Deploy

```bash
cd terraform/azure

# Login no Azure
az login

# Copiar e editar variáveis
cp terraform.tfvars.example terraform.tfvars
# Edite terraform.tfvars com seus valores

# Inicializar Terraform
terraform init

# Planejar mudanças
terraform plan

# Aplicar configuração
terraform apply
```

### Build e Push da Imagem Docker

```bash
# Obter credenciais do ACR
ACR_NAME=$(terraform output -raw acr_login_server)
ACR_USER=$(terraform output -raw acr_admin_username)
ACR_PASS=$(terraform output -raw acr_admin_password)

# Login no ACR
echo $ACR_PASS | docker login $ACR_NAME -u $ACR_USER --password-stdin

# Build da imagem
docker build -t poc-camunda-spring ..

# Tag da imagem
docker tag poc-camunda-spring:latest $ACR_NAME/poc-camunda-spring:latest

# Push da imagem
docker push $ACR_NAME/poc-camunda-spring:latest

# Reiniciar App Service
az webapp restart --name <app-service-name> --resource-group <resource-group-name>
```

### Acessar Aplicação

```bash
# Obter URL do App Service
terraform output app_service_url
```

## 🔧 Configuração

### Variáveis Importantes

**AWS:**
- `aws_region`: Região AWS (padrão: us-east-1)
- `task_cpu`: CPU para ECS task (padrão: 512)
- `task_memory`: Memória para ECS task (padrão: 1024)
- `zeebe_gateway_address`: Endereço do Zeebe Gateway

**Azure:**
- `location`: Região Azure (padrão: East US)
- `acr_name`: Nome do ACR (deve ser único globalmente)
- `app_service_name`: Nome do App Service (deve ser único globalmente)
- `app_service_plan_sku`: SKU do plano (padrão: B1)

## 🗑️ Destruir Recursos

```bash
# AWS
cd terraform/aws
terraform destroy

# Azure
cd terraform/azure
terraform destroy
```

## 💰 Custos Estimados

**AWS (us-east-1):**
- ECS Fargate: ~$15-30/mês
- ALB: ~$20/mês
- Total: ~$35-50/mês

**Azure (East US):**
- App Service B1: ~$13/mês
- ACR Basic: ~$5/mês
- Total: ~$18/mês

## 📝 Notas

- Certifique-se de que os nomes de recursos sejam únicos globalmente (ACR, App Service)
- Configure o Zeebe Gateway address apropriadamente
- Use HTTPS em produção
- Configure autenticação e autorização adequadas
