# Backend for Frontend (BFF) — Salud RedNorte

Este módulo **no** es el API Gateway. El **gateway** solo enruta peticiones. El **BFF** conoce las necesidades del frontend y **combina** varias llamadas a los microservicios (a través del gateway) en respuestas más simples para la UI.

## Rol en la arquitectura

```
React (Vite)  →  BFF (8097)  →  API Gateway (8080)  →  microservicios
```

Ventajas típicas: menos idas y vueltas desde el navegador, DTOs adaptados al portal, evolución del backend sin romper cada pantalla.

## Requisitos

- Java 17
- API Gateway y microservicios levantados (o al menos gateway + MS que expongan `/pacientes` y `/api/notifications/pending`).

## Variables de entorno

| Variable | Valor por defecto | Descripción |
|----------|-------------------|-------------|
| `BFF_SERVER_PORT` | `8097` | Puerto del BFF |
| `BFF_DOWNSTREAM_BASE_URL` | `http://localhost:8080` | Base URL del API Gateway |

## Ejecutar

```powershell
cd bff
.\mvnw.cmd spring-boot:run
```

## Endpoints

- `GET http://localhost:8097/api/portal/resumen` — JSON con `pacientes`, `notificacionesPendientes`, `resumen` (totales) y `errores` (si algún downstream falló).

## Salud

- `GET http://localhost:8097/actuator/health`
