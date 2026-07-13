# Guia Paso a Paso — AWS Academy Console

## Pre-requisitos

1. **Docker Desktop** abierto y corriendo
2. **AWS Academy** — accede desde https://awsacademy.instructure.com/ → selecciona el módulo → "Start Lab" → click en "AWS Details" → "AWS Console"

---

## FASE 0: Obtener credenciales de la lab

1. En AWS Academy, haz click en tu lab
2. Click en **"AWS Details"** (panel derecho)
3. Copia:
   - **AWS Access Key ID**
   - **AWS Secret Access Key**
   - **Default region** (normalmente `us-east-1`)
4. Click en **"AWS Console"** para abrir la consola

> CAPTURA: Pantalla de AWS Details con las credenciales

---

## FASE 1: VPC (Red)

### 1.1 Crear VPC
1. Busca **VPC** en la barra de búsqueda
2. Click en **"Create VPC"**
3. Configura:
   - Name tag: `insforge-vpc`
   - IPv4 CIDR: `10.0.0.0/16`
4. Click **"Create VPC"**

> CAPTURA: VPC creada con ID

### 1.2 Crear Subnets
1. En el menú izquierdo → **Subnets** → **"Create subnet"**
2. Crea estas 4 subnets:

| Nombre | CIDR | AZ | Tipo |
|---|---|---|---|
| `insforge-public-a` | `10.0.1.0/24` | us-east-1a | Public |
| `insforge-public-b` | `10.0.2.0/24` | us-east-1b | Public |
| `insforge-private-a` | `10.0.10.0/24` | us-east-1a | Private |
| `insforge-private-b` | `10.0.20.0/24` | us-east-1b | Private |

> CAPTURA: Las 4 subnets creadas

### 1.3 Internet Gateway
1. Menú → **Internet Gateways** → **"Create internet gateway"**
2. Name: `insforge-igw`
3. **Attach to VPC** → selecciona `insforge-vpc`

> CAPTURA: IGW creado y adjunto

### 1.4 NAT Gateway
1. Menú → **NAT Gateways** → **"Create NAT gateway"**
2. Configura:
   - Name: `insforge-nat`
   - Subnet: `insforge-public-a`
   - Connectivity type: Public
3. Click **"Create NAT gateway"**
4. Espera ~2 min hasta que el estado sea **"Available"**

> CAPTURA: NAT Gateway disponible

### 1.5 Route Tables
1. Menú → **Route Tables** → **"Create route table"**
2. Crea 2 route tables:

**Public Route Table:**
- Name: `insforge-public-rt`
- VPC: `insforge-vpc`
- Click **"Create"**
- Click **"Edit routes"** → **"Add route"**
  - Destination: `0.0.0.0/0`
  - Target: Internet Gateway → `insforge-igw`
- Click **"Save"**
- Click **"Edit subnet associations"** → selecciona `insforge-public-a` y `insforge-public-b`

**Private Route Table:**
- Name: `insforge-private-rt`
- VPC: `insforge-vpc`
- Click **"Create"**
- Click **"Edit routes"** → **"Add route"**
  - Destination: `0.0.0.0/0`
  - Target: NAT Gateway → `insforge-nat`
- Click **"Save"**
- Click **"Edit subnet associations"** → selecciona `insforge-private-a` y `insforge-private-b`

> CAPTURA: Ambas route tables con sus asociaciones

---

## FASE 2: Security Groups

1. Menú → **Security Groups** → **"Create security group"**
2. Crea estos 4 SGs:

**SG ALB:**
- Name: `insforge-alb-sg`
- VPC: `insforge-vpc`
- Inbound rules:
  - Type: All traffic, Source: `0.0.0.0/0`

**SG Backend:**
- Name: `insforge-backend-sg`
- VPC: `insforge-vpc`
- Inbound rules:
  - Type: All TCP, Source: `insforge-alb-sg`
  - Type: All TCP, Source: `insforge-backend-sg` (self-referencing)

**SG Frontend:**
- Name: `insforge-frontend-sg`
- VPC: `insforge-vpc`
- Inbound rules:
  - Type: All TCP, Source: `insforge-alb-sg`

**SG Infrastructure:**
- Name: `insforge-infra-sg`
- VPC: `insforge-vpc`
- Inbound rules:
  - Type: All TCP, Source: `insforge-backend-sg`

> CAPTURA: Los 4 Security Groups creados

---

## FASE 3: ECR (Elastic Container Registry)

1. Busca **ECR** en la barra de búsqueda
2. **"Create repository"** → crea estos repos (uno por servicio):

```
eureka-server
api-gateway
bff
ms-auth
ms-gestionpacientes
ms-optimizacion
ms-notificaciones
ms-progreso
ms-auditoria
frontend
```

- Visibility: **Private**
- Image scanning: **Scan on push**

> CAPTURA: Repositorios ECR creados

---

## FASE 4: ECS Cluster

1. Busca **ECS** en la barra de búsqueda
2. Click en **"Clusters"** → **"Create cluster"**
3. Configura:
   - Cluster name: `insforge-cluster`
   - Infrastructure: **AWS Fargate (serverless)**
   - Capacity: Fargate
4. Click **"Create"**

> CAPTURA: ECS Cluster creado

---

## FASE 5: Task Definitions

Para cada servicio, crea una Task Definition:

1. Menú → **Task Definitions** → **"Create new task definition"**
2. Configura:
   - Family: nombre del servicio
   - Launch type: **AWS Fargate**
   - Operating system: Linux
   - Architecture: x86_64
   - CPU: 0.5 vCPU (para gateway/bff) o 0.25 vCPU (para ms-*)
   - Memory: 1 GB (para gateway/bff) o 0.5 GB (para ms-*)
   - Task execution role: crear nuevo → `ecsTaskExecutionRole`
   - Container:
     - Name: nombre del servicio
     - Image URI: `<ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com/<nombre>:latest`
     - Container port: el puerto del servicio
   - Environment variables: las mismas que están en los JSON de `aws/task-definitions/`

> CAPTURA: Al menos 2-3 Task Definitions como ejemplo

### Variables de entorno por servicio

**ms-auth:**
```
SPRING_PROFILES_ACTIVE=postgres
DB_URL=jdbc:postgresql://ep-shy-hall-ai8fgqi2-pooler.c-4.us-east-1.aws.neon.tech/neondb?sslmode=require
DB_USERNAME=neondb_owner
DB_PASSWORD=<tu-password-de-neon>
RABBITMQ_HOST=rabbitmq
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=guest
RABBITMQ_PASSWORD=guest
EUREKA_CLIENT_SERVICE-URL_DEFAULTZONE=http://eureka-server:8761/eureka/
JWT_SECRET=miClaveSecretaSuperSeguraParaJWT2024SaludRedNorte
```

**api-gateway:**
```
EUREKA_CLIENT_SERVICE-URL_DEFAULTZONE=http://eureka-server:8761/eureka/
JWT_SECRET=miClaveSecretaSuperSeguraParaJWT2024SaludRedNorte
```

**bff:**
```
EUREKA_CLIENT_SERVICE-URL_DEFAULTZONE=http://eureka-server:8761/eureka/
JWT_SECRET=miClaveSecretaSuperSeguraParaJWT2024SaludRedNorte
BFF_AUTH_BASE_URL=lb://ms-auth
```

**redis:** (sin variables, solo imagen)

**rabbitmq:** (sin variables, solo imagen)

---

## FASE 6: Build y Push de imágenes

### 6.1 Preparar credenciales AWS
1. Abre PowerShell
2. Instala AWS CLI si no lo tienes:
   ```powershell
   # Descarga desde https://aws.amazon.com/cli/ e instala
   ```
3. Configura:
   ```powershell
   aws configure
   # Access Key ID: <de AWS Academy>
   # Secret Access Key: <de AWS Academy>
   # Region: us-east-1
   # Output: json
   ```

### 6.2 Login a ECR
```powershell
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin <ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com
```

### 6.3 Build y push cada servicio
```powershell
# Frontend
cd Fullstack-III-EFT-Frontend
docker build -t frontend:latest .
docker tag frontend:latest <ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com/frontend:latest
docker push <ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com/frontend:latest

# Cada microservicio
cd ..\ms-auth
mvn clean package -DskipTests
docker build -t ms-auth:latest .
docker tag ms-auth:latest <ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com/ms-auth:latest
docker push <ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com/ms-auth:latest

# Repetir para: api-gateway, bff, eureka-server, ms-gestionpacientes, ms-optimizacion, ms-notificaciones, ms-progreso, ms-auditoria
```

> CAPTURA: Docker push exitoso

---

## FASE 7: ALB (Application Load Balancer)

1. Busca **EC2** → **Load Balancers** → **"Create Load Balancer"**
2. Selecciona **Application Load Balancer**
3. Configura:
   - Name: `insforge-alb`
   - Scheme: **Internet-facing**
   - VPC: `insforge-vpc`
   - Subnets: `insforge-public-a`, `insforge-public-b`
   - Security group: `insforge-alb-sg`
4. **Listener and routing:**
   - Default: Forward to → crear target group → `tg-frontend` (port 80, HTTP)
5. Click **"Create load balancer"**
6. Espera ~3 min hasta que el estado sea **"Active"**
7. Copia el **DNS name** del ALB

> CAPTURA: ALB creado con DNS name

---

## FASE 8: Target Groups y Listener Rules

1. EC2 → **Target Groups** → crea Target Groups para cada servicio:

| Nombre | Target type | Port | Health check path |
|---|---|---|---|
| `tg-eureka` | IP | 8761 | `/actuator/health` |
| `tg-gateway` | IP | 8080 | `/actuator/health` |
| `tg-bff` | IP | 8097 | `/actuator/health` |
| `tg-auth` | IP | 8087 | `/actuator/health` |
| `tg-gp` | IP | 8083 | `/actuator/health` |
| `tg-opt` | IP | 8084 | `/actuator/health` |
| `tg-notif` | IP | 8085 | `/actuator/health` |
| `tg-prog` | IP | 8086 | `/actuator/health` |
| `tg-aud` | IP | 8088 | `/actuator/health` |

2. En el ALB → **Listeners** → **Add listener:**
   - Path: `/api/auth/*` → Forward to `tg-auth`
   - Path: `/api/portal/*` → Forward to `tg-bff`
   - Path: `/api/auditoria/*` → Forward to `tg-aud`
   - Path: `/api/notificaciones/*` → Forward to `tg-notif`
   - Path: `/api/optimizacion/*`, `/api/citas/*`, `/api/medicos/*` → Forward to `tg-opt`
   - Path: `/api/pacientes/*`, `/api/lista-espera/*` → Forward to `tg-gp`
   - Path: `/progreso/*` → Forward to `tg-prog`
   - Path: `/api/*` → Forward to `tg-gateway`

> CAPTURA: Listener rules configuradas

---

## FASE 9: ECS Services

Para cada servicio:

1. ECS → **Clusters** → `insforge-cluster` → **"Create"** (service)
2. Configura:
   - Service name: nombre del servicio
   - Launch type: **Fargate**
   - Number of tasks: **1**
   - Deployment type: Rolling update
   - VPC: `insforge-vpc`
   - Subnets: `insforge-private-a`, `insforge-private-b`
   - Security groups: `insforge-backend-sg` (o `insforge-frontend-sg` para frontend)
   - Load balancer: Application Load Balancer
   - Target group: el TG correspondiente
   - Container port: el puerto del servicio
3. Click **"Create"**

**Orden de creación:**
1. `redis` (sin ALB)
2. `rabbitmq` (sin ALB)
3. `eureka-server` (sin ALB, primero)
4. Esperar 2 min
5. `api-gateway`
6. `bff`
7. `ms-auth`
8. `ms-gestionpacientes`
9. `ms-optimizacion`
10. `ms-notificaciones`
11. `ms-progreso`
12. `ms-auditoria`
13. `frontend`

> CAPTURA: Al menos 3-4 servicios corriendo

---

## FASE 10: Autoscaling

1. ECS → **Clusters** → `insforge-cluster` → selecciona un servicio
2. Click en la pestaña **"Auto Scaling"**
3. Click **"Create"**:
   - Minimum: 1
   - Maximum: 3 (o 4 para api-gateway)
   - Target tracking: CPU 70%
4. Repite para cada servicio backend

> CAPTURA: Autoscaling configurado en al menos 2 servicios

---

## FASE 11: Verificar

1. Abre el DNS del ALB en el navegador: `http://<ALB-DNS>`
2. Deberías ver la pantalla de login
3. Login con `admin` / `admin123`
4. Navega por las secciones

> CAPTURA: Login exitoso y funcionando

---

## FASE 12: GitHub Actions (CI/CD)

1. Ve a tu repo en GitHub
2. Settings → Secrets and variables → Actions
3. Crea estos secrets:
   - `AWS_ACCESS_KEY_ID`: tu access key de AWS Academy
   - `AWS_SECRET_ACCESS_KEY`: tu secret key
   - `AWS_ACCOUNT_ID`: tu account ID (lo sacas de `aws sts get-caller-identity`)
4. Haz push a main:
   ```powershell
   git add -A
   git commit -m "feat: ECS Fargate deployment"
   git push origin main
   ```
5. Ve a la pestaña **"Actions"** en GitHub → deberías ver el workflow corriendo

> CAPTURA: Workflow de GitHub Actions exitoso

---

## Capturas mínimas para la evaluación

| # | Captura | Qué mostrar |
|---|---|---|
| 1 | VPC | VPC + 4 subnets |
| 2 | Security Groups | Los 4 SGs con reglas |
| 3 | ECR | Repositorios creados |
| 4 | ECS Cluster | Cluster Fargate |
| 5 | Task Definitions | 2-3 ejemplos |
| 6 | Docker push | Output del push exitoso |
| 7 | ALB | ALB activo con DNS |
| 8 | ECS Services | Todos los servicios corriendo |
| 9 | Autoscaling | Configurado en 2+ servicios |
| 10 | Frontend | Login funcionando |
| 11 | GitHub Actions | Workflow exitoso |
| 12 | CloudWatch Logs | Logs de un servicio |
