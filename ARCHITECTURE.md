# Arquitectura — RedNorte / InsForge

## Diagrama de Arquitectura

```mermaid
graph TB
    subgraph Cliente["Client Layer"]
        F["Frontend React<br/>(Vite :5173)"]
    end

    subgraph Gateway["Gateway Layer"]
        BFF["Backend for Frontend<br/>(:8097)"]
        GW["API Gateway<br/>(:8080)"]
        E["Eureka Server<br/>(:8761)"]
    end

    subgraph MS["Microservicios"]
        AUTH["ms-auth<br/>(:8087)"]
        GP["ms-gestionpacientes<br/>(:8083)"]
        OPT["ms-optimizacion<br/>(:8084)"]
        NOT["ms-notificaciones<br/>(:8085)"]
        PRO["ms-progreso<br/>(:8086)"]
        AUD["ms-auditoria<br/>(:8088)"]
    end

    subgraph ASYNC["Mensajería Asíncrona"]
        RMQ["RabbitMQ<br/>(:5672)"]
        EXCH_A["exchange: salud.auditoria.exchange"]
        EXCH_N["exchange: salud.notificaciones.exchange"]
    end

    subgraph DATA["Persistencia y Cache"]
        DB["Neon PostgreSQL<br/>(única instancia)"]
        REDIS["Redis<br/>(:6379)"]
    end

    F -- HTTP/JSON --> BFF
    BFF -- HTTP/JSON --> GW
    GW -- lb://ms-auth --> AUTH
    GW -- lb://ms-listas-espera --> GP
    GW -- lb://ms-optimizacion --> OPT
    GW -- lb://ms-notificaciones --> NOT
    GW -- lb://ms-progreso --> PRO
    GW -- lb://ms-auditoria --> AUD

    AUTH -- Feign --> GP
    GP -- Feign --> NOT
    GP -- Feign --> OPT
    OPT -- Feign --> NOT
    OPT -- Feign --> GP

    AUTH -. publish .-> EXCH_A
    GP -. publish .-> EXCH_A
    OPT -. publish .-> EXCH_A
    EXCH_A -. consume .-> AUD

    GP -. publish .-> EXCH_N
    OPT -. publish .-> EXCH_N
    EXCH_N -. consume .-> NOT

    RMQ --- EXCH_A
    RMQ --- EXCH_N

    AUTH -- JDBC --> DB
    GP -- JDBC --> DB
    OPT -- JDBC --> DB
    NOT -- JDBC --> DB
    PRO -- JDBC --> DB
    AUD -- JDBC --> DB

    GP -- Redis --> REDIS

    AUTH -.->|Register| E
    GW -.->|Register| E
    BFF -.->|Register| E
    GP -.->|Register| E
    OPT -.->|Register| E
    NOT -.->|Register| E
    PRO -.->|Register| E
    AUD -.->|Register| E
```

## Puertos

| Servicio | Puerto | Propósito |
|---|---|---|
| Frontend (Vite) | 5173 | SPA React |
| BFF | 8097 | Backend for Frontend, auth proxy, agregación |
| API Gateway | 8080 | Enrutamiento, balanceo de carga |
| Eureka Server | 8761 | Service Discovery |
| ms-auth | 8087 | Autenticación JWT, registro |
| ms-gestionpacientes | 8083 | Pacientes, lista de espera |
| ms-optimizacion | 8084 | Optimización de citas (Strategy Pattern) |
| ms-notificaciones | 8085 | Notificaciones push/email |
| ms-progreso | 8086 | Progreso de pacientes |
| ms-auditoria | 8088 | Auditoría de eventos |
| RabbitMQ | 5672 / 15672 | Mensajería asíncrona |
| Redis | 6379 | Caching (usado por ms-gestionpacientes) |

## Stack Tecnológico

| Capa | Tecnología |
|---|---|
| Frontend | React 19, Vite 8, Tailwind CSS v4, Lucide React, Axios |
| Backend | Spring Boot 3.4.1, Java 17 |
| Base de datos | Neon PostgreSQL (1 instancia compartida vía Flyway) |
| Cache | Redis 7 |
| Mensajería | RabbitMQ 3 |
| Service Discovery | Eureka |
| Gateway | Spring Cloud Gateway |
| Testing Frontend | Vitest 4, Testing Library, jsdom |
| Testing Backend | JUnit 5, Mockito, JaCoCo |

## Comunicación

### Síncrona (Feign / HTTP)
```
ms-auth → ms-gestionpacientes  (validación de pacientes)
ms-gestionpacientes → ms-notificaciones  (notificar evento)
ms-gestionpacientes → ms-optimizacion  (consultar citas)
ms-optimizacion → ms-notificaciones  (notificar optimización)
ms-optimizacion → ms-gestionpacientes  (consultar lista de espera)
```

### Asíncrona (RabbitMQ)
| Exchange | Publishers | Consumer |
|---|---|---|
| `salud.auditoria.exchange` | ms-auth, ms-gestionpacientes, ms-optimizacion | ms-auditoria |
| `salud.notificaciones.exchange` | ms-gestionpacientes, ms-optimizacion | ms-notificaciones |

### Persistencia
- Todos los microservicios usan la **misma instancia Neon PostgreSQL** con tablas separadas vía Flyway
- Solo ms-gestionpacientes usa Redis para caching

## Frontend

- **Sin React Router** — navegación por estado en `App.jsx` con `activeSection`
- **httpClient.js** con baseURL `http://localhost:8097` e interceptors JWT
- **Vite proxy** configurado para desarrollo (`/api` → `localhost:8080`)
- **Tema Tailwind v4** con colores Material Design 3 en `src/index.css`
- **Componentes** en `src/componentes/`
- **Tests** junto a cada componente/API (Vitest, 248 tests, 25 archivos)
