# Contexto para Agentes IA — InsForge (RedNorte)

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

## Bugs / Notas
- Los tests de API esperan rutas sin prefijo `/api` pero el código real las usa con `/api`
- El httpClient apunta a `localhost:8097` (BFF); verificar puerto si cambia
- `docker-compose.yml` levanta PostgreSQL + RabbitMQ + Redis
