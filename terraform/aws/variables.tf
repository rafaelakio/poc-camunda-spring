variable "aws_region" {
  description = "AWS region"
  type        = string
  default     = "us-east-1"
}

variable "project_name" {
  description = "Project name"
  type        = string
  default     = "poc-camunda-spring"
}

variable "environment" {
  description = "Environment (dev, staging, prod)"
  type        = string
  default     = "dev"
}

variable "task_cpu" {
  description = "CPU units for ECS task"
  type        = string
  default     = "512"
}

variable "task_memory" {
  description = "Memory for ECS task"
  type        = string
  default     = "1024"
}

variable "desired_count" {
  description = "Desired number of ECS tasks"
  type        = number
  default     = 1
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
