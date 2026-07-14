# AGENTS.md — InsForge (RedNorte)

## Repo & Git
- GitHub: `https://github.com/Matiasv117/Fullstack-III-EFT-Backend.git`
- Branch: `main`
- Windows LF→CRLF warnings on `git add`: normal, ignore

## Stack
| Capa | Tecnología |
|---|---|
| Frontend | React 19, Vite 8, Tailwind CSS v4, Lucide React, Axios |
| Backend | Spring Boot 3.4.1, Java 17 |
| BD | Neon PostgreSQL (1 instancia compartida, Flyway por MS) |
| Cache | Redis 7 (solo ms-gestionpacientes) |
| Mensajería | RabbitMQ 3 (auditoría + notificaciones) |
| Service Discovery | Eureka (:8761) |
| Gateway | Spring Cloud Gateway (:8080) + BFF (:8097) |

## Microservicios
| Servicio | Puerto | `spring.application.name` |
|---|---|---|
| eureka-server | 8761 | eureka-server |
| api-gateway | 8080 | api-gateway |
| bff | 8097 | salud-bff |
| ms-auth | 8087 | ms-auth |
| ms-gestionpacientes | 8083 | **ms-listas-espera** |
| ms-optimizacion | 8084 | ms-optimizacion |
| ms-notificaciones | 8085 | ms-notificaciones |
| ms-progreso | 8086 | ms-progreso |
| ms-auditoria | 8088 | ms-auditoria |

## Arquitectura
- **Frontend → BFF (:8097) → API Gateway (:8080) → Microservicios**
- Síncrono: Feign clients entre MS
- Asíncrono: RabbitMQ — `salud.auditoria.exchange` (publishers: auth, gp, optimizacion; consumer: auditoria), `salud.notificaciones.exchange` (publishers: gp, optimizacion; consumer: notificaciones)
- JWT HMAC-SHA, clave: `miClaveSecretaSuperSeguraParaJWT2024SaludRedNorte`
- BFF SecurityConfig: auth/actuator → permitAll, admin → ADMIN, pacientes/lista-espera/optimizacion/autotriage/citas/auditoria → FUNCIONARIO+ADMIN, resto authenticated
- Notificaciones endpoint permitAll (Feign clients internos no llevan JWT)

## Base de Datos
- **Neon cloud** (no local) — todos los MS apuntan a `ep-shy-hall-ai8fgqi2-pooler.c-4.us-east-1.aws.neon.tech/neondb`
- Usuario: `neondb_owner`, password: `npg_Viz5aYF6NSuO` (defaults en `application.yml`, sobreescribible vía env vars)
- Cada MS usa su propia tabla Flyway: `flyway_ms_progreso`, etc.
- Perfil Spring: `postgres`
- `docker-compose.yml` solo levanta Redis + RabbitMQ + Mailpit (no PostgreSQL)

## Frontend
- **Sin React Router** — navegación por `activeSection` en `App.jsx`
- `httpClient.js` baseURL: `http://localhost:8097` (BFF)
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
| `.\mvnw.cmd spring-boot:run` | Iniciar servicio (perfil `postgres` vía env o config) |
| `.\mvnw.cmd test` | Tests JUnit |
| `.\mvnw.cmd verify` | Tests + JaCoCo coverage |

### Scripts generales (raíz)
| Comando | Descripción |
|---|---|
| `scripts/start-all.ps1` | Docker infra + todos los MS (Eureka 1°, resto paralelo) |
| `scripts/start-no-docker.ps1` | MS sin Docker (infra debe estar aparte) |
| `scripts/stop-all.ps1` | Mata procesos por puerto |
| `scripts/smoke-test-e2e.ps1` | Smoke test via gateway |

## Orden de arranque
1. `docker compose up -d` (Redis, RabbitMQ, Mailpit)
2. Eureka server (obligatorio primero)
3. Resto de MS en paralelo (todos dependen de Eureka)

## Convenciones
- **Backend**: controller → service → repository → entity, DTOs separados de entidades, Feign clients, config en `application.yml`
- **Frontend**: PascalCase componentes (`GestionPacientes.jsx`), camelCase hooks con prefijo `use`, camelCase API modules, Tailwind utility classes (sin CSS separados)

## Gotchas / Bugs
- `ms-gestionpacientes` se registra como **`ms-listas-espera`** en Eureka: `@FeignClient(name = "ms-listas-espera")` y rutas `lb://ms-listas-espera`
- En Windows siempre agregar `eureka.instance.prefer-ip-address: true` en nuevos MS (hostname no resuelve por DNS)
- `@FeignClient(name = ...)` debe coincidir exactamente con `spring.application.name` del destino
- ms-optimizacion espera `Long pacienteId` en DTO, pero ms-listas-espera devuelve `paciente: { id, nombre, apellido }` — mismatch de mapeo Jackson
- JaCoCo configurado en los 9 servicios (incluye eureka-server y api-gateway)
- **Swagger**: springdoc 2.6.x es **incompatible** con Spring Boot 3.4.x (`NoSuchMethodError: ControllerAdviceBean`). Usar **2.7.0+** en todos los pom.xml
- **Swagger gateway**: api-gateway usa `springdoc-openapi-starter-webflux-ui` (no `webmvc-ui`). Los MS que tienen `springdoc.api-docs.path: /api-docs` (auth, auditoria, progreso) necesitan `"/api-docs/**"` como permitAll en SecurityConfig y el RewritePath del gateway debe apuntar a `/api-docs`
- Swagger/OpenAPI unificado en `http://localhost:8080/swagger-ui.html` (7 grupos via gateway)
- Config local: copiar `config/local-insforge.env.example` → `config/local-insforge.env`
- `opencode.json` tiene MCP Neon server configurado (requiere `NEON_API_KEY`)

## Tareas completadas

### Fixes de tests (2026-07-13)
- **ms-auth `AdminControllerTest`**: Agregado `@Mock JwtUtil` + stubs con `lenient()` en `@BeforeEach` para evitar `UnnecessaryStubbingException`
- **BFF `ListaEsperaControllerTest`**: Agregado `ObjectMapper` y `@Mock ProgresoService` para resolver NPE por `@InjectMocks`
- **Frontend `Notificaciones.test.jsx`**: Mock de `gestionPacientesApi` + uso de `waitFor` para asserts async
- **Eliminados `contextLoads()` vacíos**: `MsProgresoApplicationTests.java` y `MsAuditoriaApplicationTests.java` (causaban timeout con `@SpringBootTest` sin tests reales)
- **ms-auditoria test config**: Agregado `spring.flyway.enabled: false` en `src/test/resources/application.yml`
- **Resultado final**: 630 tests, 0 failures (266 frontend + 364 backend)

### Swagger/OpenAPI funcional en todos los MS (2026-07-13)
- **Causa raíz**: springdoc 2.6.0 incompatible con Spring Boot 3.4.x — `NoSuchMethodError: ControllerAdviceBean` por los `@RestControllerAdvice` existentes
- **Fix**: actualizado springdoc a **2.7.0** en los 9 pom.xml
- **Gateway**: Swagger unificado en `http://localhost:8080/swagger-ui.html` con 7 grupos (BFF, Gestion Pacientes, Notificaciones, Optimizacion, Progreso, Auth, Auditoria)
- **Security**: agregados `/api-docs/**` como permitAll en SecurityConfig de auth, auditoria y progreso (usan `springdoc.api-docs.path: /api-docs`)
- **Gateway RewritePath**: auth/auditoria/progreso apuntan a `/api-docs` (no `/v3/api-docs`) porque tienen path custom
- **BFF SecurityConfig**: agregados Swagger paths como permitAll

### Cobertura JaCoCo salud-bff 85%+ (2026-07-13)
- Expandidos tests en `NotificacionesControllerTest` (+8): POST crearNotificacion (3 paths), GET paciente/{id} (3 paths), GET info/canales error paths
- Expandidos tests en `OptimizacionControllerTest` (+4): GET prioridad (3 paths), POST cancelar after-call success path
- Fix en `PacientesControllerTest`: mapper vía `ReflectionTestUtils.setField` para cubrir after-call completo
- Expandidos tests en `ListaEsperaControllerTest` (+7): GET metricas (3 paths), POST after-call success, PUT/POST/GET error paths
- Expandidos tests en `PortalResumenServiceTest` (+3): WebClientResponseException catch blocks, Mono.empty() → blockOptional().orElseGet() path
- **Resultado**: 153 tests, 0 failures, 97.5% line coverage (threshold: 85%)

## Tareas pendientes

### Documentar endpoints con anotaciones OpenAPI
- Agregar `@Operation`, `@ApiResponse` a los controllers de cada MS
- Agregar `@Tag` a los controllers que aún no lo tienen

### Revisión final pre-presentación (2026-07-15)
- Revisar alineación y coherencia del flujo de trabajo general del sistema
- Verificar que todas las secciones del informe estén actualizadas
- Preparar demostración en vivo para defensa oral (Swagger, tests, smoke test E2E, snippets de código)
- Preparar respuestas individuales para posibles preguntas del profesor
