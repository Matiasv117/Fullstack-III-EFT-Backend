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
| api-gateway / bff | 8080 | Gateway, BFF, auth proxy |
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
- Tests junto a cada componente/API

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

## Base de Datos
- Hosteada en Neon (no local) — todos los microservicios apuntan a Neon ahora
- MCP server configurado en `opencode.json` para consultas directas (requiere `NEON_API_KEY`)
- Conexión verificada: TCP al host de Neon responde en puerto 5432
- Todos los ms usan la misma instancia `neondb` con tablas Flyway separadas

## Pendientes (próximos pasos)
1. ~~Arreglar `httpClient.js` baseURL (`localhost:8097` no coincide~~ — Completado
2. ~~Configurar microservicios para usar Neon~~ — Completado
3. ~~Corregir tests API (rutas `/api` vs sin `/api`)~~ — Completado
4. ~~Corregir 11 tests de `App.test.jsx`~~ — Completado
5. ~~Probar autenticación end-to-end~~ — Completado
6. ~~Probar docker-compose completo~~ — Completado
7. ~~Revisar cobertura de tests (JaCoCo) con `./mvnw verify`~~ — Completado
8. ~~Agregar tests a eureka-server (no tiene tests)~~ — Completado
9. ~~Verificar docker-compose.full.yml para despliegue completo~~ — Completado
10. ~~Probar inicio con `scripts/start-all.ps1`~~ — Completado
11. Ejecutar `mvn verify` en todos los servicios (verificar cobertura post-cambio)
12. Verificar tests frontend (`npm test` en Frontend)

## Bugs / Notas
- ~~Los tests de API esperan rutas sin prefijo `/api` pero el código real las usa con `/api`~~ — Corregido
- El httpClient apunta a `localhost:8097` (BFF); verificar puerto si cambia
- `docker-compose.yml` levanta solo Redis + RabbitMQ (PostgreSQL es Neon cloud)
- Al hacer `git add` en Windows pueden aparecer warnings de `LF will be replaced by CRLF` — es normal, no afecta
- ~~Los 11 tests de App.test.jsx fueron corregidos~~
