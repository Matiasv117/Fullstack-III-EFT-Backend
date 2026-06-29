# eureka-server

Servicio de descubrimiento (Service Discovery) de RedNorte usando Netflix Eureka Server.

## Qué hace

- Registro y descubrimiento de microservicios
- Health checks periódicos
- Dashboard web de monitoreo de instancias
- Balanceo de carga del lado del cliente

## Tecnologías

- Spring Boot 3.4.1
- Java 17
- Netflix Eureka Server
- SpringDoc OpenAPI (Swagger)
- JaCoCo (cobertura)

## Ejecutar el servicio

```bash
mvn spring-boot:run
```

El servicio arranca en:

- `http://localhost:8761/`

## Endpoints

| Ruta | Descripción |
|---|---|
| `/` | Dashboard Eureka |
| `/eureka/apps` | API REST de registro (JSON/XML) |
| `/eureka/apps/{appId}` | Detalle de instancia |
| `/actuator/health` | Health check |
| `/actuator/info` | Información del servicio |
| `/swagger-ui.html` | Swagger UI |
| `/api-docs` | OpenAPI spec |

## Configuración

- Puerto: 8761
- No requiere base de datos (registro en memoria)
- Self-preservation habilitado para entornos productivos

## Variables de entorno

- `EUREKA_PORT`: Puerto del servidor (default: 8761)

## Pruebas

```bash
mvn test
```

## Estructura base

```text
src/main/java/com/saludrednorte/eurekaserver/
└── EurekaServerApplication.java
```
