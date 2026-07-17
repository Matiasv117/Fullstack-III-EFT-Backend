# InsForge RedNorte

Plataforma de gestion hospitalaria desplegada en **AWS EKS** con arquitectura de microservicios.

## Arquitectura

```
Internet
  |
  v
[NLB Frontend :80]  [NLB API Gateway :8080]
  |                       |
  v                       v
[Frontend (React)]  [API Gateway (Spring Cloud Gateway)]
                          |
            +------+------+------+------+ 
            |      |      |      |      |
            v      v      v      v      v
        ms-auth  ms-gp  ms-opt  ms-notif
        :8087   :8083  :8084   :8085
            |      |      |      |
            +------+------+------+---> Neon PostgreSQL
```

## Microservicios

| Servicio | Puerto | Descripcion |
|---|---|---|
| `api-gateway` | 8080 | Enrutamiento, JWT validation, CORS |
| `ms-auth` | 8087 | Autenticacion, registro, roles |
| `ms-gestionpacientes` | 8083 | Pacientes, lista de espera |
| `ms-optimizacion` | 8084 | Citas, medicos, horarios, optimizacion |
| `ms-notificaciones` | 8085 | Notificaciones email |
| `frontend` | 80 (prod) | React SPA, nginx |

## Stack

| Capa | Tecnologia |
|---|---|
| Frontend | React 19, Vite 8, Tailwind CSS v4, Lucide React |
| Backend | Spring Boot 3.4.1, Java 17 |
| BD | Neon PostgreSQL (Flyway por MS) |
| Cache | ConcurrentMapCacheManager (in-memory) |
| Comunicacion | Feign clients con URLs directas + env vars |
| Gateway | Spring Cloud Gateway (Netty) |
| Auth | JWT HMAC-SHA |
| Deploy | AWS EKS (Auto Mode), ECR, NLB |
| CI/CD | GitHub Actions |

## Requisitos

- Java 17 + Maven (o `./mvnw`)
- Node.js 18+
- Docker
- kubectl + AWS CLI (para deploy en EKS)

## Desarrollo Local

### Backend

```powershell
# Iniciar los 5 microservicios en paralelo
.\scripts\start-all.ps1

# Detener todos
.\scripts\stop-all.ps1

# Smoke test E2E
.\scripts\smoke-test-e2e.ps1
```

### Frontend

```bash
cd Fullstack-III-EFT-Frontend
npm install
npm run dev    # http://localhost:5173
```

### Docker Compose (alternativa)

```bash
docker-compose up -d   # PostgreSQL + Mailpit + 5 microservicios
```

### Neon (PostgreSQL cloud)

Todos los MS usan una instancia Neon compartida con esquemas separados por Flyway.

1. Copiar `config/local-insforge.env.example` a `config/local-insforge.env`
2. Cargar variables: `. .\scripts\load-insforge-env.ps1`
3. Iniciar servicios en la misma ventana

## Tests

### Backend (270 tests, 0 fallas)

```powershell
# Por servicio
cd ms-auth; .\mvnw.cmd test

# Todos con cobertura
.\mvnw.cmd verify
```

### Frontend (266 tests)

```bash
cd Fullstack-III-EFT-Frontend
npm test
npm run test:coverage
```

### Cobertura JaCoCo

| Servicio | Lineas | Funciones |
|---|---|---|
| ms-optimizacion | 85%+ | 80%+ |
| ms-auth | 87%+ | 80%+ |
| ms-gestionpacientes | 85%+ | 80%+ |
| ms-notificaciones | 91%+ | 80%+ |

## Deploy en AWS EKS

### Estructura de.infra

| Recurso | Detalle |
|---|---|
| Cluster | `insforge-eks` (Auto Mode, us-east-1) |
| VPC | 2 public + 2 private subnets, 2 NAT GWs |
| ECR | 6 repos (1 por servicio + frontend) |
| NLB | 2 internet-facing (frontend :80, api-gateway :8080) |
| Nodos | Auto-provisioned por Karpenter (amd64) |

### URLs de produccion

```
Frontend:    http://k8s-insforge-frontend-*.elb.us-east-1.amazonaws.com
API Gateway: http://k8s-insforge-apigatew-*.elb.us-east-1.amazonaws.com:8080
```

### Comandos de deploy

```powershell
# Login a ECR
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin 366092663280.dkr.ecr.us-east-1.amazonaws.com

# Build y push (ejemplo: api-gateway)
cd api-gateway
docker build -t 366092663280.dkr.ecr.us-east-1.amazonaws.com/api-gateway:latest .
docker push 366092663280.dkr.ecr.us-east-1.amazonaws.com/api-gateway:latest

# Apply manifests K8s
kubectl apply -f k8s/namespace.yml
kubectl apply -f k8s/secrets.yml
kubectl apply -f k8s/ms-auth.yml
kubectl apply -f k8s/ms-gestionpacientes.yml
kubectl apply -f k8s/ms-optimizacion.yml
kubectl apply -f k8s/ms-notificaciones.yml
kubectl apply -f k8s/api-gateway.yml
kubectl apply -f k8s/frontend.yml

# Verificar
kubectl get pods -n insforge
kubectl get svc -n insforge
```

### CI/CD

GitHub Actions (`.github/workflows/deploy.yml`):
1. Build y test (backend + frontend)
2. Build Docker images
3. Push a ECR
4. Deploy a EKS via kubectl
5. Smoke test

### Variables de entorno (K8s)

| Variable | Servicio | Descripcion |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | Todos | `k8s` |
| `DB_URL` | Backend | Neon PostgreSQL JDBC URL |
| `DB_USERNAME` | Backend | `neondb_owner` |
| `DB_PASSWORD` | Backend | Neon password |
| `JWT_SECRET` | auth, gateway | Clave HMAC-SHA |
| `MS_LISTAS_ESPERA_URL` | auth, gateway | `http://ms-gestionpacientes:8083` |
| `MS_OPTIMIZACION_URL` | gateway, gestionpacientes | `http://ms-optimizacion:8084` |
| `MS_NOTIFICACIONES_URL` | gateway, gestionpacientes, optimizacion | `http://ms-notificaciones:8085` |
| `MS_AUTH_URL` | gateway | `http://ms-auth:8087` |

## Feign Clients

| Origen | Cliente | Destino | Env Var |
|---|---|---|---|
| ms-auth | PacienteClient | ms-gestionpacientes:8083 | `MS_LISTAS_ESPERA_URL` |
| ms-gestionpacientes | CitaClient | ms-optimizacion:8084 | `MS_OPTIMIZACION_URL` |
| ms-gestionpacientes | NotificationClient | ms-notificaciones:8085 | `MS_NOTIFICACIONES_URL` |
| ms-optimizacion | PacienteClient | ms-gestionpacientes:8083 | `MS_LISTAS_ESPERA_URL` |
| ms-optimizacion | ListaEsperaClient | ms-gestionpacientes:8083 | `MS_LISTAS_ESPERA_URL` |
| ms-optimizacion | NotificationClient | ms-notificaciones:8085 | `MS_NOTIFICACIONES_URL` |

## Autenticacion

JWT HMAC-SHA con clave compartida. El API Gateway valida tokens en cada request.

### Flujo

1. `POST /api/auth/login` -> valida credenciales -> devuelve `{ token, type: "Bearer" }`
2. Frontend almacena token en `localStorage`
3. API Gateway valida token con `JwtTokenValidator` (filtro global)

### Usuarios por defecto

| Usuario | Contrasena | Rol |
|---|---|---|
| `admin` | `admin123` | `ROLE_ADMIN` |
| `funcionario` | `funcionario123` | `ROLE_FUNCIONARIO` |
| `paciente` | `paciente123` | `ROLE_PACIENTE` |

## Autores

| Nombre | GitHub |
|---|---|
| Matias Vargas | [@Matiasv117](https://github.com/Matiasv117) |
| Benjamin Ibanez | [@beibanezv](https://github.com/beibanezv) |
| Fabian Reyes | [@FabianReyes02](https://github.com/FabianReyes02) |
