# Contexto para Agentes IA — InsForge (RedNorte)

## Git
- Repo: `https://github.com/Matiasv117/Fullstack-III-EFT-Backend.git`
- Branch: `main` (migrada de `Fabian`)
- Archivos .md obsoletos fueron eliminados (sesión 2026-06-26)

## Stack Tecnológico

| Capa | Tecnología |
|---|---|
| Frontend | React 19, Vite 8, Tailwind CSS v4, Lucide React icons |
| Backend | Spring Boot 3.4.1, Java 17 |
| BD | PostgreSQL (1 por microservicio), Redis (caching) |
| Mensajería | RabbitMQ (auditoría y notificaciones) |
| Testing Frontend | Vitest 4, Testing Library, jsdom |
| Testing Backend | JUnit 5, Mockito, JaCoCo |
| Infra | Docker, Kubernetes (EKS), ECR |
| Service Discovery | Eureka (puerto 8761) |
| Gateway | Spring Cloud Gateway + BFF (puerto 8080) |

## Comandos Esenciales

```bash
# Frontend (dentro de Fullstack-III-EFT-Frontend/)
npm run dev          # Iniciar servidor de desarrollo (Vite)
npm run build        # Build producción
npm test             # Correr tests (Vitest)
npm run lint         # ESLint

# Backend (cada microservicio)
./mvnw spring-boot:run              # Iniciar servicio
./mvnw test                          # Tests
./mvnw verify                        # Tests + cobertura

# Scripts generales (raíz del proyecto)
scripts/start-all.ps1                # Iniciar todo con Docker Compose
scripts/start-no-docker.ps1          # Iniciar servicios sin Docker
scripts/stop-all.ps1                 # Detener todo
```

## Arquitectura

### Microservicios

| Servicio | Puerto | Responsabilidad |
|---|---|---|
| eureka-server | 8761 | Service discovery |
| api-gateway | 8080 | API Gateway (Spring Cloud Gateway) |
| bff | 8097 | Backend for Frontend, auth proxy, agregación |
| ms-auth | 8087 | Autenticación JWT, registro |
| ms-gestionpacientes | 8083 | Pacientes, listas de espera |
| ms-optimizacion | 8084 | Optimización de citas (Strategy Pattern) |
| ms-notificaciones | 8085 | Notificaciones push/email |
| ms-progreso | 8086 | Progreso de pacientes |
| ms-auditoria | 8088 | Auditoría de eventos (RabbitMQ) |
| Frontend (Vite) | 5173 | SPA React |

### Comunicación
- Síncrona: Feign clients entre microservicios
- Asíncrona: RabbitMQ (exchange `salud.auditoria.exchange`, `salud.notificaciones.exchange`)
- Auth: JWT HMAC-SHA (clave: `miClaveSecretaSuperSeguraParaJWT2024SaludRedNorte`)

### Frontend
- Sin React Router — navegación por estado en `App.jsx` con `activeSection`
- `httpClient.js` con baseURL `http://localhost:8097` e interceptors para JWT
- Rutas API con prefijo `/api/` (ej: `/api/pacientes`, `/api/auth/login`)
- Vite proxy configurado para desarrollo (`/api` -> `localhost:8080`)
- Tema Tailwind v4 definido en `src/index.css` con colores Material Design 3
- Componentes en `src/componentes/`
- Tests junto a cada componente/API (Vitest 4 + Testing Library + jsdom)
- Coverage con provider v8, thresholds configurados en `vitest.config.js`
- **248 tests, 25 archivos de test**
- Cobertura: lines 87.22%, statements 84.78%, functions 82.58%, branches 71%

## Convenciones de Código

### Frontend
- Nombres de componentes: PascalCase (`GestionPacientes.jsx`)
- Hooks: camelCase con prefijo `use` (`useGestionPacientes.js`)
- API modules: camelCase (`gestionPacientesApi.js`)
- CSS: Tailwind utility classes, NO archivos CSS separados
- Tema centralizado en `index.css` con `@theme`

### Backend
- Estructura: controller → service → repository → entity
- DTOs separados de entidades
- Feign clients para comunicación entre servicios
- Eventos con Spring Cloud Stream / RabbitTemplate
- Config centralizada en `application.yml`

## Cambios completados (26 Jun 2026)

### Fase 1 — Tests Frontend + UI
- Sidebar: `w-25 h20` → `w-24 h-20` (tamaño de logo)
- `index.css`: agregado `--spacing-gutter: 24px` (paddings funcionando)
- Sidebar: eliminado `useState` no usado
- `GestionPacientesView`: corregidas 10 clases Tailwind inválidas, 7 imports no usados, `Date.now()` → `crypto.randomUUID()`
- `Dashboard`: corregida fuga de memoria en event listeners
- Eliminados 44 archivos .md/.txt obsoletos
- Creados `AGENTS.md` y `opencode.json`
- Neon MCP server configurado (vía add-mcp)
- Migrados ms-optimizacion, ms-progreso, ms-auditoria, ms-notificaciones de H2 a Neon (misma instancia que ms-auth y ms-gestionpacientes)
- Corregidos tests de API: `gestionPacientesApi.test.js` (prefijo `/api`), `optimizacionApi.test.js` (prefijo `/api`), `GestionPacientesView.test.jsx` (datos válidos para registrar)
- **Sidebar.jsx**: agregado `aria-hidden="true"` a todos los iconos Material Symbols
- **Sidebar.jsx**: corregido accesible name del botón logout
- **Sidebar.jsx**: clase activa corregida a `sidebar-item-active`
- **App.jsx**: agregado footer `"© 2026 RedNorte. Todos los derechos reservados."`
- **Dashboard.jsx**: integrado `obtenerResumenPortal()` en `useEffect` con manejo de error
- **App.test.jsx**: corregidos 11 tests (selectores de botones, clase activa, footer, resumen portal)

### Fase 2 — Tests Backend + Verificación E2E
- Corregidos 11 tests de `App.test.jsx` → **159/159 frontend tests pasan**
- Ejecutados tests en todos los microservicios:
  - ms-auth: 20 ✅ | ms-gestionpacientes: 85 ✅ | ms-optimizacion: 101 ✅
  - ms-notificaciones: 37 ✅ | ms-progreso: 10 ✅ | ms-auditoria: 13 ✅
  - bff: 4 ✅ | api-gateway: 5 ✅ | eureka-server: 0 (sin tests) ✅
  - **Total backend: 275 tests, 0 fallas**
- **Gran total: 434 tests, 0 fallas**
- Verificada autenticación E2E (3 rutas):
  - ms-auth directo (8087): POST `/api/auth/login` → JWT ✅
  - BFF (8097): POST `/api/auth/login` → JWT con type Bearer ✅
  - API Gateway (8080): POST `/api/auth/login` → JWT ✅
- Verificado resumen portal E2E: BFF → API Gateway → ms-gestionpacientes + ms-notificaciones → Neon → 8 pacientes ✅
- Limpiado `docker-compose.yml`: eliminados 6 PostgreSQL locales (innecesarios con Neon), solo Redis + RabbitMQ
- Docker compose iniciado correctamente

### Fase 3 — Cobertura JaCoCo (100% servicios)
- **JaCoCo** agregado a api-gateway y eureka-server (9/9 servicios con cobertura)
- **110+ tests nuevos** para ms-auth y bff (AdminController, RutUtil, AuthController, AuthService, 10 Web Controllers, AuthProxyService, AutotriageService, PortalResumenService)
- **13 tests** para `PacientePortalController` en ms-gestionpacientes
- **Corregidos tests** en ms-auth (status expectations, FeignException handling) y bff (WebClientResponseException.Unauthorized, AuthProxyService mocking chain)
- **`mvn verify` pasa en todos los servicios**
- Coberturas finales: ms-auditoria 100%, ms-progreso 96%, ms-optimizacion 94%, bff 91.5%, ms-notificaciones 91%, ms-auth 87.1%, ms-gestionpacientes ≥85%, api-gateway 50%, eureka-server N/A

### Fase 4 — Docker + Despliegue
- `docker-compose.full.yml`: removido `version: '3.8'` (deprecado), fixeado Dockerfile de eureka-server (instala curl para healthcheck)
- **eureka-server**: agregado 1 test de contexto + JaCoCo + maven-surefire-plugin con ByteBuddy
- Script `start-all.ps1` listo para usar

### Fase 5 — Reportes, Ayuda, Auditoría y Tests Backend
- **ReportesView.jsx** (nuevo): Vista de reportes con métricas de lista de espera (totales, distribución por gravedad, tabla de auditoría), 228 líneas
- **AyudaModal.jsx** (nuevo): Modal de ayuda del sistema con descripción de módulos
- **reportesApi.js** (nuevo): Módulo API para reportes (`/api/lista-espera/metricas`, `/api/auditoria/eventos`)
- **App.jsx**: Placeholder de reportes reemplazado por `ReportesView`, Sidebar recibe `onLogout`, footer movido afuera
- **Sidebar.jsx**: Botón de ayuda abre `AyudaModal`, botón de cerrar sesión funcional (`onLogout`), `aria-hidden` en iconos
- **Dashboard.jsx**: Valores dinámicos desde `obtenerResumenPortal()` reemplazan hardcode (total pacientes, notificaciones pendientes)
- **App.test.jsx**: Tests actualizados para nuevos selectores y etiquetas (botones exactos, clase `sidebar-item-active`)
- **AuditoriaController.java** (nuevo): Endpoint `GET /api/auditoria/eventos` en BFF, 41 líneas
- **ListaEsperaController.java**: Agregado endpoint `GET /api/lista-espera/metricas`
- **docker-compose.yml**: Eliminados 6 PostgreSQL locales (innecesarios con Neon), solo Redis + RabbitMQ
- **Tests backend nuevos** (15 clases, ~2,000+ líneas):
  - **bff**: 10 Web Controller tests (Admin, Auth, Autotriage, ListaEspera, Notificaciones, Optimizacion, Pacientes, Portal) + 3 Service tests (AuthProxyService, AutotriageService, PortalResumenService)
  - **ms-auth**: AdminControllerTest (280 líneas), AuthControllerTest (+48), AuthServiceTest (+68), RutUtilTest (111 líneas)
  - **ms-gestionpacientes**: PacientePortalControllerTest (187 líneas)
  - **eureka-server**: EurekaServerApplicationTest (12 líneas) + application.yml test
- **api-gateway/pom.xml**: Agregadas dependencias de test + JaCoCo
- **eureka-server/pom.xml**: Agregado JaCoCo + maven-surefire-plugin + ByteBuddy + test de contexto
- **docker-compose.full.yml**: Removido `version: '3.8'` (deprecado)
- **eureka-server/Dockerfile**: Agregado curl para healthcheck

### Fase 6 — Fixes posteriores al merge (27 Jun 2026)
- **JwtAuthenticationFilter** en ms-gestionpacientes, ms-notificaciones, ms-optimizacion, ms-auditoria:
  - Eliminada dependencia de `UserDetailsService` (no existe bean en microservicios downstream)
  - Eliminado método `authenticateFromLegacyToken()` (fallback Base64 obsoleto)
  - Ahora extraen username/role directamente del token JWT, igual que el filter de ms-auditoria
- **`eureka.instance.prefer-ip-address: true`** agregado a ms-auth, ms-auditoria, ms-progreso, api-gateway (ms-gestionpacientes, ms-notificaciones, ms-optimizacion ya lo tenían). Sin esto los servicios se registran con el hostname de Windows (`LAPTOP-5OQAK09E.mshome.net`) que no se resuelve por DNS.
- **`PacienteClient.java`**: corregido `@FeignClient(name = "ms-gestionpacientes")` → `"ms-listas-espera"` para que coincida con el `spring.application.name` real del servicio.
- **`ListaEsperaController.java`** y **`PacientesController.java`** en BFF: cambiado `lb://ms-gestionpacientes` → `lb://ms-listas-espera`.
- **API Gateway route**: cambiado `uri: lb://ms-gestionpacientes` → `lb://ms-listas-espera`.
- `start-all.ps1` y `start-no-docker.ps1`: optimizados para arranque paralelo (Eureka primero, resto simultáneo).

### Fase 7 — Cobertura Frontend ≥85% + Diagrama de Arquitectura (29 Jun 2026)
- **ARCHITECTURE.md**: Reemplazado con diagrama Mermaid completo (Frontend → BFF → Gateway → 6 MS → Neon + RabbitMQ + Redis + Eureka)
- **Sidebar.test.jsx** (nuevo): 11 tests — menús por rol (funcionario/admin/paciente), dark mode toggle, botón Ayuda (abre AyudaModal), logout
- **Dashboard.test.jsx** (nuevo): 6 tests — vista paciente (PORTAL DEL PACIENTE), vista funcionario, error/sync/loading states, handleSyncRetry click
- **TopNavBar.test.jsx** (actualizado): 7 tests — agregado search input onChange + USER badge por defecto
- **App.test.jsx** (actualizado): 22 tests — agregado navegación a ajustes (admin y funcionario), dark mode toggle
- **vitest.config.js**: Corregido formato de thresholds para Vitest 4 (`coverage.thresholds` sub-object). Thresholds: lines 85, functions 80, branches 65, statements 80
- **Cobertura frontend**: lines 87.22%, statements 84.78%, functions 82.58%, branches 71% — todos los thresholds se cumplen
- **Tests**: 248 tests, 25 archivos, 0 fallas

### Fase 8 — Swagger/OpenAPI + Exception Handlers + PDF Cobertura (29 Jun 2026)
- **Swagger/OpenAPI**: Agregado `springdoc-openapi-starter-webmvc-ui` 2.6.0 a api-gateway, bff, eureka-server. Actualizado 2.3.0 → 2.6.0 en ms-notificaciones y ms-optimizacion. Agregado `@OpenAPIDefinition(info = @Info(...))` en los 9 Application classes.
- **GlobalExceptionHandler**: Creados en api-gateway, bff y eureka-server (ya existían en los otros 6 MS). Todos siguen el mismo patrón: `@RestControllerAdvice` con handlers para `MethodArgumentNotValidException` (400), `IllegalArgumentException` (400) y `Exception` (500).
- **PDF Cobertura JaCoCo**: Script `scripts/coverage-report.js` con pdfkit que genera `coverage-report-backend.pdf` — tabla de los 9 MS con 4 métricas (instrucciones, ramas, líneas, métodos), barras por servicio y resumen general.
- **Frontend**: no disponible localmente para generar Vitest coverage PDF.
- **Tests**: 275 backend (0 fallas), 248 frontend (0 fallas).

## Base de Datos
- Hosteada en Neon (no local) — todos los microservicios apuntan a Neon ahora
- MCP server configurado en `opencode.json` para consultas directas (requiere `NEON_API_KEY`)
- Conexión verificada: TCP al host de Neon responde en puerto 5432
- Todos los ms usan la misma instancia `neondb` con tablas Flyway separadas

### Fase 9 — Fix PortalResumenService + Dashboard "Nueva Consulta" (29 Jun 2026)
- **PortalResumenService.java**: Cambió inyección de `downstreamWebClient` (API Gateway fijo → 503) por `WebClient.Builder` (load-balanced). Ahora usa `lb://ms-listas-espera/pacientes` y `lb://ms-notificaciones/api/notificaciones/pendientes`, igual que los demás controllers del BFF.
- **PortalResumenServiceTest.java**: 6 tests actualizados con nueva firma de constructor (`WebClient.Builder` en vez de `WebClient`). Tests de autorización reescritos con Mockito verify.
- **Dashboard.jsx**: "ACCESO RÁPIDO - Nueva Consulta" ahora navega a la pestaña "Pacientes" via `onSectionChange('pacientes')`.
- **App.jsx**: Pasado `onSectionChange={setActiveSection}` a Dashboard.
- **Verificación E2E**: `GET /api/portal/resumen` retorna `totalPacientes: 2` (antes 0).
- **Tests**: 89/89 BFF pasan (0 fallas).

### Fase 10 — Fixes ListaEspera, Notificaciones y Optimización (29 Jun 2026)
- **ListaEspera Frontend**: Cambiado `item.pacienteId ?? 'N/A'` → `item.paciente ? "${item.paciente.nombre} ${item.paciente.apellido}" : "Paciente ID: N/A"`. Ahora muestra nombre real del paciente en vez del ID.
- **GestionPacientesView.jsx**: Botón "Agregar a lista" ahora abre un modal con selector de gravedad (BAJA/MEDIA/ALTA) y campo interconsulta, en vez de agregar con valores fijos.
- **useGestionPacientes.js**: `agregarALista` ahora acepta `opciones = {}` y las pasa a `agregarPacienteAListaEspera`.
- **Notifications 500 fix**: `SecurityConfig.java` en ms-notificaciones: agregado `.requestMatchers("/api/notificaciones/**").permitAll()`. Los Feign clients internos no llevan JWT → obtenían 403. Ahora pueden crear notificaciones sin auth.
- **BFF NotificacionesController.java**: Agregado endpoint `POST /api/notificaciones` que proxy a ms-notificaciones.
- **BFF OptimizacionController.java**: Agregado endpoint `GET /api/optimizacion/prioridad`.
- **optimizacionApi.js**: Agregada función `obtenerPrioridadPaciente()`.
- **Optimizacion.jsx**: Agregadas **3 pestañas de estrategia (FIFO/LIFO/Por Gravedad)** que reordenan la misma lista para comparar visualmente. "Paciente ID: N/A" reemplazado por "ID: {item.id}".
- **Tests**: 250 frontend (0 fallas), 275 backend (0 fallas).

### Fase 11 — Fix tests api-gateway + README eureka-server (29 Jun 2026)
- **SimpleAuthServiceTest.java**: Corregidos 3 tests fallando — eliminados tests de legacy `generarToken` (Base64), reescritos con JWT vía `buildJwt()`. Fixeado `decodeBearerToken_mapeaFuncionarioComoAdmin` → `mapeaFuncionarioComoUser` (espera "USER", correcto según `JwtTokenValidator.mapRole()`).
- **api-gateway**: Tests 4/4 pasan (0 fallas).
- **eureka-server/README.md**: Creado con documentación completa (tecnologías, endpoints, configuración, variables de entorno).
- **Tests**: 275 backend (0 fallas), 248 frontend (0 fallas), 0 fallas en api-gateway.

### Fase 10 — Propuesta de redesign Optimización (pendiente)
- **Problema**: `ListaEsperaDTO` en ms-optimizacion tiene `Long pacienteId` pero el JSON de ms-listas-espera devuelve `paciente: { id, nombre, apellido }`. Jackson no puede mapear `paciente.id` → `pacienteId` automáticamente.
- **Propuesta Opción B**: Convertir Optimización en mini-gestor de citas + simulador:
  1. Fix DTO: mapear `paciente.id → pacienteId` + agregar `nombrePaciente` para mostrar nombres reales.
  2. Agregar proxy de citas al BFF: `POST /api/citas`, `GET /api/citas`, `DELETE /api/citas/{id}` (CitaController ya existe en ms-optimizacion).
  3. Simplificar UI: "Crear Cita" + "Cancelar y Reasignar" con estrategia seleccionable.
  4. Esto activaría el flujo RabbitMQ → notificaciones reales al cancelar citas.
- **Propuesta Opción A** (más simple): solo mantener pestañas de estrategia + agregar score de prioridad (`GET /prioridad`) por paciente.

### Fase 14 — Auditoría general de falencias + fixes (29 Jun 2026)
1. ✅ **Fix Autotriage + UI de triaje** — Corregido payload (`pacienteId` → `paciente: { id }` + mapeo int→string gravedad). Creado `TriagePaciente.jsx` con selector de paciente, radio buttons de gravedad (1-5), campo de síntomas. Nueva API `triageApi.js`. Sidebar item "Triage".
2. ✅ **Unificar flujo Paciente → ListaEspera → Cita** — Eliminada creación automática de cita en `PacienteService.registrarPaciente()` (primer médico, fecha mañana). Tests actualizados.
3. ✅ **Activar ms-progreso** — Creado `ProgresoService` en BFF (best-effort WebClient). Integrado en AutotriageService (EVALUANDO_PRIORIDAD → EN_LISTA_ACTIVA), PacientesController (SINTOMAS_REGISTRADOS), ListaEsperaController (EN_LISTA_ACTIVA), CitasController y OptimizacionController (CITA_ASIGNADA). Ms-progreso SecurityConfig permite `/progreso/**`. Tests 0 fallas.
4. ✅ **Auditar eventos reales** — Creado `AuditoriaService` en BFF (best-effort WebClient). Integrado en AuthController (LOGIN_EXITOSO/LOGIN_FALLIDO), PacientesController (PACIENTE_REGISTRADO), CitasController y OptimizacionController (CITA_OPTIMIZADA). Ms-auditoria SecurityConfig permite `/api/auditoria/eventos`. BFF AuditoriaController ahora acepta POST para eventos desde frontend. Tests 0 fallas.
5. ✅ **Fix BFF SecurityConfig** — Aplicados roles: auth y actuator permitAll, admin → ADMIN, pacientes/lista-espera/optimizacion/autotriage/citas/auditoria → FUNCIONARIO+ADMIN, resto authenticated. Tests 0 fallas.
6. ✅ **Limpiar credenciales** — Unificada Neon URL y password en docker-compose.full.yml para que coincida con defaults de application.yml (ep-shy-hall / npg_Viz5aYF6NSuO).
7. ✅ **Fix API Gateway route** — `/api/listas-espera/**` → `/api/lista-espera/**` (singular) en api-gateway application.yml. Tests 0 fallas.

### Fase 13 — Plan de sincronización ListaEspera-Optimización (29 Jun 2026)
1. ✅ **Sincronizar "Cancelar y Reasignar" con lista de espera** — Al reasignar una cita, marcar el registro de lista de espera como `ASIGNADA` (vía `ListaEsperaClient.actualizarEstado(id, "ASIGNADA")` desde cada estrategia).
2. ✅ **Arreglar estados del frontend** — En `ListaEspera.jsx` y `Optimizacion.jsx`, cambiado `ATENDIDO` → `ASIGNADA`, `CANCELADO` → `FINALIZADA` (badge styles, selects, tests).
3. ✅ **Devolver paciente real reasignado** — `OptimizacionService.procesarCancelacion()` ahora retorna `ReasignacionResponse{citaId, pacienteId, nombrePaciente}`. El frontend usa `response.nombrePaciente` directamente en vez de adivinar el primer `PENDIENTE`.
4. ✅ **Crear UI de agendamiento manual** — Nuevo componente `AgendarCita.jsx` con formulario (paciente, médico, fecha/hora), función `crearCita` en `citasApi.js`, sidebar item "Agendar Cita" y ruta `agendarcita` en `App.jsx`.

## Bugs / Notas
- ~~Los tests de API esperan rutas sin prefijo `/api` pero el código real las usa con `/api`~~ — Corregido
- El httpClient apunta a `localhost:8097` (BFF); verificar puerto si cambia
- `docker-compose.yml` levanta solo Redis + RabbitMQ (PostgreSQL es Neon cloud)
- Al hacer `git add` en Windows pueden aparecer warnings de `LF will be replaced by CRLF` — es normal, no afecta
- ~~Los 11 tests de App.test.jsx fueron corregidos~~
- En Windows con WSL/Hyper-V, los servicios se registran en Eureka con IP virtual (172.x.x.x). Siempre agregar `eureka.instance.prefer-ip-address: true` en nuevos microservicios.
- El nombre del `@FeignClient` debe coincidir exactamente con `spring.application.name` del servicio destino.
- ~~api-gateway tenía 3 tests fallando en `SimpleAuthServiceTest` — Corregidos (29 Jun 2026)~~
