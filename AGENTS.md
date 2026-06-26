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
- Sidebar: `w-25 h20` → `w-24 h-20` (tamaño de logo)
- `index.css`: agregado `--spacing-gutter: 24px` (paddings funcionando)
- Sidebar: eliminado `useState` no usado
- `GestionPacientesView`: corregidas 10 clases Tailwind inválidas, 7 imports no usados, `Date.now()` → `crypto.randomUUID()`
- `Dashboard`: corregida fuga de memoria en event listeners
- Eliminados 44 archivos .md/.txt obsoletos
- Creados `AGENTS.md` y `opencode.json`
- Neon MCP server configurado (vía add-mcp)
- Migrados ms-optimizacion, ms-progreso, ms-auditoria, ms-notificaciones de H2 a Neon (misma instancia que ms-auth y ms-gestionpacientes)
- Corregidos tests de API: `gestionPacientesApi.test.js` (prefijo `/api`), `optimizacionApi.test.js` (prefijo `/api`), `GestionPacientesView.test.jsx` (datos válidos para registrar) — 15 tests arreglados, de 29 fallas → 11 fallas restantes

## Tests
- Frontend: **148 tests pasan, 11 fallan** (159 total) — mejora desde 29 fallas
- Tests corregidos (sesión 2026-06-26):
  - `gestionPacientesApi.test.js` (26 tests): prefijo `/api` agregado a todas las rutas → ✅ todos pasan
  - `optimizacionApi.test.js` (3 tests): prefijo `/api` agregado a todas las rutas → ✅ todos pasan
  - `GestionPacientesView.test.jsx` (1 test): datos de paciente válidos para que `registrar()` sea llamado → ✅ todos pasan
  - `App.test.jsx` (14 tests): 11 fallan aún — son preexistentes, NO relacionados con prefijo `/api` (ver Pendientes)
- Backend: usar `./mvnw test` en cada microservicio

## Base de Datos
- Hosteada en Neon (no local) — todos los microservicios apuntan a Neon ahora
- MCP server configurado en `opencode.json` para consultas directas (requiere `NEON_API_KEY`)
- Conexión verificada: TCP al host de Neon responde en puerto 5432
- Todos los ms usan la misma instancia `neondb` con tablas Flyway separadas

## Pendientes (próximos pasos)
1. ~~Arreglar `httpClient.js` baseURL (`localhost:8097` no coincide~~ — Sí coincide: el BFF escucha en 8097)
2. ~~Configurar microservicios para usar Neon~~ — Completado
3. ~~Corregir tests API (rutas `/api` vs sin `/api`)~~ — Completado (gestionPacientesApi, optimizacionApi, GestionPacientesView)
4. **Corregir 11 tests de `App.test.jsx`** — fallos preexistentes NO relacionados con `/api`:
   - 3 tests de resumen portal: `obtenerResumenPortal` nunca se llama (ningún componente en el árbol la invoca)
   - 4 tests de navegación: botones del Sidebar tienen accesible name con formato `"iconText"` (ej: `"groupPacientes"`) pero tests buscan `"Gestión de Pacientes"`
   - 1 test de footer: `"© 2026 RedNorte"` no se renderiza
   - 1 test de sección activa: clase CSS `navItemActive` no existe en el Sidebar actual
   - 2 tests de renderizado básico: textos no encontrados en la vista autenticada
5. Probar autenticación end-to-end
6. Probar docker-compose completo

## Bugs / Notas
- ~~Los tests de API esperan rutas sin prefijo `/api` pero el código real las usa con `/api`~~ — Corregido
- El httpClient apunta a `localhost:8097` (BFF); verificar puerto si cambia
- `docker-compose.yml` levanta PostgreSQL + RabbitMQ + Redis
- Al hacer `git add` en Windows pueden aparecer warnings de `LF will be replaced by CRLF` — es normal, no afecta
- App.test.jsx (11 fallas): tests escritos para versión anterior de la UI — mockean `portalApi.obtenerResumenPortal` que ningún componente consume; los accesible names del Sidebar son `"iconText"` pero tests buscan texto legible
