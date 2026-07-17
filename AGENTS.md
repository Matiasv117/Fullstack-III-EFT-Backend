# AGENTS.md — InsForge (RedNorte)

## Repo & Git
- GitHub: `https://github.com/Matiasv117/Fullstack-III-EFT-Backend.git`
- Branch: `deploy` (rama de producción, pipeline se dispara aquí)
- `main` = código estable
- Windows LF→CRLF warnings on `git add`: normal, ignore

## Stack
| Capa | Tecnología |
|---|---|
| Frontend | React 19, Vite 8, Tailwind CSS v4, Lucide React, Axios |
| Backend | Spring Boot 3.4.1, Java 17 |
| BD | Neon PostgreSQL (1 instancia compartida, Flyway por MS) |
| Cache | ConcurrentMapCacheManager (in-memory, reemplaza Redis) |
| Mensajería | Feign clients síncronos (reemplaza RabbitMQ) |
| Service Discovery | URLs directas + env vars (reemplaza Eureka) |
| Gateway | Spring Cloud Gateway (:8080) |
| Orquestación | AWS EKS (Manual Mode, Managed Node Groups) |
| CI/CD | GitHub Actions → ECR → K8s |

## Arquitectura simplificada (post-simplificación 2026-07-16)

El proyecto fue simplificado de 7 microservicios a 5+frontend para facilitar el despliegue en AWS EKS. Se eliminaron: eureka-server, BFF, ms-auditoria, ms-progreso, Redis, RabbitMQ.

### Flujo de comunicación
**Frontend → Nginx (proxy) → API Gateway (:8080) → Microservicios**

- Nginx proxy: `/api/*` → `http://api-gateway:8080` (mismo pod)
- Síncrono: Feign clients entre MS (con `url` param + env vars)
- Sin Eureka: API Gateway usa URLs directas configurable vía env vars
- Sin RabbitMQ: Notificaciones se envían vía Feign (ms-gestionpacientes/ms-optimizacion → ms-notificaciones)
- Sin Redis: Cache in-memory (ConcurrentMapCacheManager)
- JWT HMAC-SHA, clave: `miClaveSecretaSuperSeguraParaJWT2024SaludRedNorte`
- Notificaciones endpoint permitAll (Feign clients internos no llevan JWT)

## Microservicios
| Servicio | Puerto | `spring.application.name` | Estado |
|---|---|---|---|
| api-gateway | 8080 | api-gateway | Activo — rutas directas (sin lb://) |
| ms-auth | 8087 | ms-auth | Activo |
| ms-gestionpacientes | 8083 | **ms-listas-espera** | Activo |
| ms-optimizacion | 8084 | ms-optimizacion | Activo |
| ms-notificaciones | 8085 | ms-notificaciones | Activo |
| Frontend | 5173 (dev) / 80 (prod) | — | Activo |

### Eliminados (no desplegar)
- eureka-server (:8761) — eliminado
- BFF (:8097) — eliminado
- ms-auditoria (:8088) — eliminado
- ms-progreso (:8086) — eliminado
- Redis — eliminado
- RabbitMQ — eliminado

## Base de Datos
- **Neon cloud** (no local) — todos los MS apuntan a `ep-shy-hall-ai8fgqi2-pooler.c-4.us-east-1.aws.neon.tech/neondb`
- Usuario: `neondb_owner`, password: `npg_Viz5aYF6NSuO` (defaults en `application.yml`, sobreescribible vía env vars)
- Cada MS usa su propia tabla Flyway: `flyway_ms_auth`, `flyway_ms_listas_espera`, `flyway_ms_optimizacion`, `flyway_ms_notificaciones`
- `docker-compose.yml` levanta: frontend + 5 MS + PostgreSQL + Mailpit en red `insforge-net`

## Frontend
- **Sin React Router** — navegación por `activeSection` en `App.jsx`
- `httpClient.js` baseURL: `''` (vacio — URLs relativas para que Nginx proxy maneje `/api/*`)
- Vite dev proxy: `/api` → `http://localhost:8080` (gateway)
- Tema Tailwind v4 en `src/index.css` con `@theme`
- Componentes en `src/componentes/`, APIs en `src/api/`, hooks en `src/hooks/`

## Comandos

### Frontend (dir: `Fullstack-III-EFT-Frontend/`)
| Comando | Descripción |
|---|---|
| `npm run dev` | Vite dev server (:5173) |
| `npm test` | Vitest (todos los tests) |
| `npm run test:coverage` | Vitest + coverage (thresholds: lines 85, functions 80, branches 65, statements 80) |
| `npm run lint` | ESLint |
| `npm run build` | Build producción |

### Backend (cada MS, usar `.\mvnw.cmd` en Windows)
| Comando | Descripción |
|---|---|
| `.\mvnw.cmd spring-boot:run` | Iniciar servicio |
| `.\mvnw.cmd test` | Tests JUnit |
| `.\mvnw.cmd verify` | Tests + JaCoCo coverage |

### Scripts generales (raíz)
| Comando | Descripción |
|---|---|
| `scripts/start-all.ps1` | 5 MS en paralelo (sin Eureka, sin Docker infra) |
| `scripts/stop-all.ps1` | Mata procesos por puerto (8080, 8087, 8083, 8084, 8085) |
| `scripts/smoke-test-e2e.ps1` | Smoke test via gateway |

### AWS EKS
| Comando | Descripción |
|---|---|
| `kubectl get pods -n insforge` | Ver pods |
| `kubectl get svc -n insforge` | Ver servicios |
| `kubectl logs -n insforge -l app=<name> -f` | Logs de un MS |
| `kubectl delete pod -n insforge -l app=<name>` | Self-healing demo |
| `kubectl rollout restart deployment <name> -n insforge` | Reiniciar deployment |
| `kubectl scale deployment --all --replicas=0 -n insforge` | Apagar todos (ahorrar) |
| `kubectl scale deployment <name> -n insforge --replicas=1` | Encender uno |

## Orden de arranque (actualizado)
1. `scripts/start-all.ps1` — inicia los 5 MS en paralelo
2. `npm run dev` en Fullstack-III-EFT-Frontend/
3. Abrir http://localhost:5173

**Ya no se necesita:** Docker (Redis/RabbitMQ), Eureka, BFF

## Convenciones
- **Backend**: controller → service → repository → entity, DTOs separados de entidades, Feign clients, config en `application.yml`
- **Frontend**: PascalCase componentes (`GestionPacientes.jsx`), camelCase hooks con prefijo `use`, camelCase API modules, Tailwind utility classes (sin CSS separados)

## Feign Clients (URLs directas)

Todos los Feign clients usan `url` param con env var para desacoplar de Eureka:

| MS origen | Cliente Feign | URL destino | Env var |
|---|---|---|---|
| ms-auth | PacienteClient | ms-gestionpacientes:8083 | `MS_LISTAS_ESPERA_URL` |
| ms-gestionpacientes | CitaClient | ms-optimizacion:8084 | `MS_OPTIMIZACION_URL` |
| ms-gestionpacientes | NotificationClient | ms-notificaciones:8085 | `MS_NOTIFICACIONES_URL` |
| ms-optimizacion | PacienteClient | ms-gestionpacientes:8083 | `MS_LISTAS_ESPERA_URL` |
| ms-optimizacion | ListaEsperaClient | ms-gestionpacientes:8083 | `MS_LISTAS_ESPERA_URL` |
| ms-optimizacion | NotificationClient | ms-notificaciones:8085 | `MS_NOTIFICACIONES_URL` |

## Gotchas / Bugs
- `ms-gestionpacientes` se registra como **`ms-listas-espera`** en `spring.application.name` (legacy name, no cambiar)
- `@FeignClient(name = ...)` debe coincidir exactamente con `spring.application.name` del destino
- ms-optimizacion espera `Long pacienteId` en DTO, pero ms-listas-espera devuelve `paciente: { id, nombre, apellido }` — mismatch de mapeo Jackson
- JaCoCo configurado en los 5 servicios activos
- **Swagger**: springdoc 2.7.0+ (incompatible con 2.6.x en Spring Boot 3.4.x)
- **Swagger gateway**: api-gateway usa `springdoc-openapi-starter-webflux-ui` (no `webmvc-ui`)
- Config local: copiar `config/local-insforge.env.example` → `config/local-insforge.env`

## Deploy AWS EKS (2026-07-17)

### Cluster
- **Nombre**: insforge-manual
- **Region**: us-east-1
- **Modo**: Manual (Managed Node Groups) — NO Auto Mode
- **K8s Version**: 1.31
- **VPC**: `vpc-0d1c6629c36bb3556` (4 subnets, 2 NAT GWs)
- **Account**: 366092663280
- **Costo**: ~$8.64/día (EKS $2.40 + EC2 $3.00 + NAT GWs $2.16 + NLBs $1.08)
- **IAM Roles**: `LabEksClusterRole` (cluster) + `LabEksNodeRole` (nodos)
- **Node Group**: `insforge-nodes` (t3.medium, min 2, desired 3, max 4)
- **CloudWatch**: Habilitado (OTel Container Insights)
- **Vocareum STS**: Credenciales temporales (empiezan con `ASIA`), requieren `AWS_SESSION_TOKEN`

### ECR Repos
- `366092663280.dkr.ecr.us-east-1.amazonaws.com/{api-gateway,ms-auth,ms-gestionpacientes,ms-optimizacion,ms-notificaciones,frontend}:latest`

### K8s Resources
- Namespace: `insforge`
- Secrets: `insforge-secrets` (DB_URL, DB_USERNAME, DB_PASSWORD, JWT_SECRET)
- 6 Deployments + 6 Services (2 LoadBalancer NLB, 4 ClusterIP)

### NLBs (internet-facing, URLs permanentes)
- **Frontend**: `http://a796e2d22281e4840a1d71e4253014c1-5e784c18168e743b.elb.us-east-1.amazonaws.com` (port 80)
- **API Gateway**: `http://a16febf025c6b4ad4a02eaa9f34e24d1-dfe0ff84fc128f87.elb.us-east-1.amazonaws.com:8080` (port 8080)

### Usuarios de prueba
| Usuario | Contraseña | Rol |
|---|---|---|
| `admin` | `admin123` | ROLE_ADMIN |
| `fun1` | `funcionario1` | ROLE_FUNCIONARIO |

### Ahorro de costo (nodos a 1 antes de dormir)
```bash
aws eks update-nodegroup-config --cluster-name insforge-manual --nodegroup-name insforge-nodes --scaling-config minSize=1,desiredSize=1,maxSize=1 --region us-east-1
```

### Restaurar nodos (mañana antes de presentar)
```bash
aws eks update-nodegroup-config --cluster-name insforge-manual --nodegroup-name insforge-nodes --scaling-config minSize=2,desiredSize=3,maxSize=4 --region us-east-1
```

### Mañana: solo actualizar GitHub Secrets
Los 3 secrets que cambian con cada sesión Vocareum:
- `AWS_ACCESS_KEY_ID`
- `AWS_SECRET_ACCESS_KEY`
- `AWS_SESSION_TOKEN`
`AWS_ACCOUNT_ID` (`366092663280`) NO cambia.

### Fixes aplicados durante deploy
- **api-gateway pom.xml**: agregado `spring-boot-starter-actuator` (faltaba, probes fallaban)
- **ms-notificaciones application-k8s.properties**: agregado `management.health.mail.enabled=false` (Mailpit no existe en K8s)
- **k8s manifests**: agregadas annotations NLB internet-facing
- **Subnets tags**: `kubernetes.io/role/elb=1` en públicas, `kubernetes.io/role/internal-elb=1` en privadas
- **NodeClass**: restringido a subnets privadas (nodos necesitan NAT para ECR)
- **httpClient.js**: baseURL cambiado de `http://localhost:8080` a `''` (vacio para nginx proxy)
- **httpClient.test.js**: test actualizado para baseURL vacío

## Tareas completadas

### Simplificación del proyecto (2026-07-16)
- Eliminados: eureka-server, BFF, ms-auditoria, ms-progreso, Redis, RabbitMQ
- API Gateway: rutas `lb://` → URLs directas con env vars (MS_LISTAS_ESPERA_URL, etc.)
- 6 Feign clients: agregado `url` param con default localhost + env var override
- Eureka deshabilitado en todos los MS: `eureka.client.enabled: false`
- ms-notificaciones: eliminado messaging/ (RabbitMQ), eliminada dependencia spring-amqp
- ms-gestionpacientes: eliminado messaging/ (RabbitMQ), eliminado RedisCacheManager → ConcurrentMapCacheManager
- ms-optimizacion: eliminado messaging/ (RabbitMQ), eliminado TestMessagingConfig
- Frontend: baseURL cambiado de 8097 (BFF) → 8080 (API Gateway directo) → `''` (vacio, nginx proxy)
- SecurityConfig ms-auth: actuator paths reducidos a `/actuator/health` y `/actuator/info`
- **Resultado**: 318 tests, 0 failures (266 frontend + 52 backend)

### Despliegue K8s preparado (2026-07-16)
- Dockerfiles multi-stage creados para los 5 MS + Frontend (con nginx)
- nginx.conf creado para Frontend (proxy /api → api-gateway)
- `application-k8s.yml` creado para api-gateway, ms-auth, ms-gestionpacientes, ms-optimizacion
- `application-k8s.properties` creado para ms-notificaciones
- K8s manifests creados en `k8s/` con annotations NLB internet-facing

### Deploy AWS EKS completado (2026-07-17)
- VPC creada con 4 subnets (2 públicas, 2 privadas) + 2 NAT GWs
- EKS cluster `insforge-manual` con Managed Node Groups (NO Auto Mode)
- 6 ECR repos creados, todas las imágenes push
- Todos los pods Running, 0 restarts
- 2 NLBs internet-facing activos
- Login funcional end-to-end (Frontend → Nginx → API Gateway → ms-auth)
- CI/CD pipeline `.github/workflows/deploy.yml` creado (rama `deploy`)
- Architecture diagrams: `architecture.puml`, `architecture.drawio`
- `README.md` reescrito con documentación completa

### Fixes críticos (2026-07-17, sesión nocturna)
- **SimpleAuthFilter 403 fix**: `USER_GET_PATH_PREFIXES` corregido de `/pacientes` a `/api/pacientes` (agregados `/api/pacientes`, `/api/lista-espera`, `/api/notificaciones`, `/api/citas`, `/api/medicos`, `/api/optimizacion`, `/api/reportes`, `/api/horarios`)
- **CORS fix**: `allowedOrigins` → `allowedOriginPatterns: ["*"]`, `allowedHeaders` como lista, `allowCredentials: false`
- **Gateway K8s probes**: `initialDelaySeconds` aumentado a 50/60 para startup de 38s
- **Dashboard resilience**: `.catch()` agregado a cada llamada en `Promise.all` del Dashboard.jsx
- **docker-compose.yml**: Agregado servicio `frontend` + red explícita `insforge-net` en todos los servicios
- **nginx.conf**: Verificado correcto con `proxy_set_header Authorization $http_authorization`

### Fixes de tests (2026-07-13)
- **ms-auth `AdminControllerTest`**: Agregado `@Mock JwtUtil` + stubs con `lenient()` en `@BeforeEach`
- **BFF `ListaEsperaControllerTest`**: Agregado `ObjectMapper` y `@Mock ProgresoService`
- **Frontend `Notificaciones.test.jsx`**: Mock de `gestionPacientesApi` + uso de `waitFor`
- **Eliminados `contextLoads()` vacíos**: `MsProgresoApplicationTests.java` y `MsAuditoriaApplicationTests.java`

### Swagger/OpenAPI funcional (2026-07-13)
- springdoc actualizado a 2.7.0+ en todos los pom.xml
- Gateway: Swagger unificado en `http://localhost:8080/swagger-ui.html`

## Tareas pendientes

### Pre-presentación (MAÑANA)
- [ ] Borrar `ARCHITECTURE.md` (describe arquitectura vieja con 7 MS, Eureka, BFF)
- [ ] Capturar screenshots: VPC, EKS, ECR, NLBs, pods, pipeline verde, login
- [ ] Grabar videos: self-healing, scaling, demo frontend completa
- [ ] Actualizar GitHub Secrets con nuevas credenciales Vocareum
- [ ] Restaurar nodos: `aws eks update-nodegroup-config ... --scaling-config minSize=2,desiredSize=3,maxSize=4`
- [ ] Verificar frontend accesible desde NLB URL
- [ ] Ensayar presentación 10-15 min con guía

### Post-defensa (opcional)
- [ ] Documentar endpoints con anotaciones OpenAPI
- [ ] Configurar CloudWatch logs (pendiente — incompatible con K8s actual)
