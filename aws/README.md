# Deploy InsForge en AWS ECS Fargate

## Requisitos previos

1. **AWS CLI** instalado y configurado:
   ```bash
   aws configure
   # Ingresa: Access Key ID, Secret Access Key, Region (us-east-1), Output format (json)
   ```

2. **Docker** corriendo (para build de imágenes)

3. **Java 17** y **Maven** (para compilar los microservicios)

4. **Node.js 20** (para el frontend)

## Despliegue rápido (un solo comando)

```powershell
.\aws\deploy.ps1
```

Esto ejecuta todo: VPC → ECS → ALB → Task Definitions → Services → Autoscaling → Smoke Test.

## Despliegue paso a paso

Si prefieres ejecutar cada paso manualmente:

```powershell
# 1. Crear infraestructura AWS (VPC, subnets, ECS cluster, ALB, ECR)
.\aws\01-setup-infra.ps1

# 2. Registrar Task Definitions y crear ALB + routing
.\aws\02-register-tasks-and-alb.ps1

# 3. Crear ECS Services
.\aws\03-create-services.ps1

# 4. Configurar Autoscaling
.\aws\04-autoscaling.ps1

# 5. Smoke test
.\aws\05-smoke-test.ps1
```

## Configuración de GitHub Actions

### 1. Crear usuario IAM para GitHub Actions

```bash
# Crear usuario
aws iam create-user --user-name github-actions-deploy

# Adjuntar política
aws iam attach-user-policy \
  --user-name github-actions-deploy \
  --policy-arn arn:aws:iam::$(aws sts get-caller-identity --query Account --output text):policy/GitHubActionsDeployPolicy

# Crear access key
aws iam create-access-key --user-name github-actions-deploy
```

### 2. Configurar Secrets en GitHub

En tu repo de GitHub: Settings → Secrets and variables → Actions → New repository secret:

| Secret | Valor |
|---|---|
| `AWS_ACCESS_KEY_ID` | Access Key del usuario IAM |
| `AWS_SECRET_ACCESS_KEY` | Secret Key del usuario IAM |
| `AWS_ACCOUNT_ID` | ID de tu cuenta AWS (12 dígitos) |

### 3. Crear política IAM

```bash
aws iam create-policy \
  --policy-name GitHubActionsDeployPolicy \
  --policy-document file://aws/github-actions-policy.json
```

### 4. Push a main → deploy automático

```bash
git push origin main
```

El workflow de GitHub Actions ejecutará:
1. Build de todos los servicios (paralelo)
2. Push de imágenes a ECR
3. Deploy a ECS (force new deployment)
4. Smoke test post-deploy

## Arquitectura desplegada

```
Internet
    │
    ▼
ALB (puerto 80)
    ├── /               → Frontend (nginx:80)
    ├── /api/auth/*     → ms-auth (8087)
    ├── /api/portal/*   → BFF (8097)
    ├── /api/pacientes/*→ ms-gestionpacientes (8083)
    ├── /api/optimizacion/* → ms-optimizacion (8084)
    ├── /api/notificaciones/* → ms-notificaciones (8085)
    ├── /api/auditoria/* → ms-auditoria (8088)
    ├── /progreso/*     → ms-progreso (8086)
    └── /api/*          → API Gateway (8080)
                              │
                              ▼
                        Microservicios
                        (Eureka discovery)
                              │
                    ┌─────────┼─────────┐
                    ▼         ▼         ▼
                Neon DB   Redis    RabbitMQ
```

## Autoscaling

| Servicio | Min | Max | Target CPU |
|---|---|---|---|
| api-gateway | 1 | 4 | 70% |
| bff | 1 | 3 | 70% |
| ms-auth | 1 | 3 | 70% |
| ms-gestionpacientes | 1 | 3 | 70% |
| ms-optimizacion | 1 | 2 | 70% |
| ms-notificaciones | 1 | 2 | 70% |
| ms-progreso | 1 | 2 | 70% |
| ms-auditoria | 1 | 2 | 70% |
| frontend | 1 | 3 | 70% |

## Comandos útiles

```powershell
# Ver estado de servicios
aws ecs describe-services --cluster insforge-cluster --services $(aws ecs list-services --cluster insforge-cluster --query 'serviceArns[]' --output text)

# Ver logs en tiempo real
aws logs tail /ecs/api-gateway --follow

# Forzar redeploy
aws ecs update-service --cluster insforge-cluster --service api-gateway --force-new-deployment

# Ver métricas de autoscaling
aws application-autoscaling describe-scaling-activities --service-namespace ecs --resource-id service/insforge-cluster/api-gateway

# Verificar targets del ALB
aws elbv2 describe-target-health --target-group-arn <TG_ARN>

# Eliminar todo (cuidado)
aws ecs update-service --cluster insforge-cluster --service api-gateway --desired-count 0
# ... repetir para cada servicio, luego:
aws ecs delete-cluster --cluster insforge-cluster
```

## Troubleshooting

### Servicio no arranca
```bash
aws logs tail /ecs/<service-name> --since 10m
```

### Tasks stuck in PENDING
- Verificar que Fargate tiene cuota (default: 10 vCPU)
- Verificar subnets privadas tienen NAT Gateway

### Health checks fallan
- Verificar que la app escucha en el puerto correcto
- Verificar `/actuator/health` está expuesto
- Verificar Security Groups permiten tráfico

### Eureka no registra servicios
- Eureka debe arrancar PRIMERO
- Los demás servicios esperan 120s antes de health check
- Verificar `EUREKA_CLIENT_SERVICE-URL_DEFAULTZONE` apunta a `http://eureka-server:8761/eureka/`
