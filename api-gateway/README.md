# API Gateway

API Gateway para enrutar solicitudes a los microservicios de RedNorte.

## Qué hace

- Enruta solicitudes HTTP a los microservicios correspondientes
- Descubre servicios dinámicamente a través de Eureka
- Centraliza el punto de entrada para todas las peticiones
- Valida tokens JWT antes de enrutar a los microservicios

## Tecnologías

- Spring Boot 3.4.1
- Java 17
- Spring Cloud Gateway
- Eureka Client
- JWT (jjwt 0.11.5)

## Ejecutar el servicio

Desde la carpeta `api-gateway`:

```bash
mvn spring-boot:run
```

El servicio arranca en:

- `http://localhost:8080`

## Rutas

El gateway enruta automáticamente a los microservicios registrados en Eureka usando el patrón:

```
http://localhost:8080/{nombre-servicio}/{ruta}
```

Ejemplos:

- `http://localhost:8080/api/auth/login` → ms-auth (puerto 8087)
- `http://localhost:8080/ms-auditoria/auditoria/eventos` → ms-auditoria (puerto 8088)
- `http://localhost:8080/ms-gestionpacientes/pacientes` → ms-gestionpacientes (puerto 8083)
- `http://localhost:8080/ms-notificaciones/api/notificaciones` → ms-notificaciones (puerto 8085)
- `http://localhost:8080/ms-optimizacion/citas` → ms-optimizacion (puerto 8084)
- `http://localhost:8080/ms-progreso/progreso` → ms-progreso (puerto 8086)

## Configuración

- Puerto: 8080
- Eureka: Registrado en eureka-server:8761
- Service Discovery: Habilitado (lower-case service IDs)

## Variables de entorno

No requiere variables de entorno adicionales. Configuración en `application.properties`.

## Estructura base

```text
src/main/java/saludrednorte/api_gateway/
├── config/
└── filter/
```
