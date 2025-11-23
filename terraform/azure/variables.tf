variable "location" {
  description = "Azure region"
  type        = string
  default     = "East US"
}

variable "resource_group_name" {
  description = "Resource group name"
  type        = string
  default     = "rg-poc-camunda-spring"
}

variable "project_name" {
  description = "Project name"
  type        = string
  default     = "poc-camunda-spring"
}

variable "acr_name" {
  description = "Azure Container Registry name (must be globally unique)"
  type        = string
  default     = "acrpoccamunda"
}

variable "app_service_name" {
  description = "App Service name (must be globally unique)"
  type        = string
  default     = "app-poc-camunda-spring"
}

variable "app_service_plan_sku" {
  description = "App Service Plan SKU"
  type        = string
  default     = "B1"
}

variable "app_service_always_on" {
  description = "Keep app always on"
  type        = bool
  default     = false
}

variable "environment" {
  description = "Environment (dev, staging, prod)"
  type        = string
  default     = "dev"
}

variable "zeebe_gateway_address" {
  description = "Zeebe gateway address"
  type        = string
  default     = "127.0.0.1:26500"
}

variable "tags" {
  description = "Tags to apply to resources"
  type        = map(string)
  default = {
    Project     = "poc-camunda-spring"
    Environment = "dev"
    ManagedBy   = "Terraform"
  }
}
