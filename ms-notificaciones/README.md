# ms-notificaciones

Microservicio de notificaciones de RedNorte.

## Qué hace

- crea notificaciones
- consulta notificaciones por ID
- consulta notificaciones por paciente
- lista notificaciones pendientes
- marca notificaciones como enviadas
- expone información básica del servicio
- se registra en Eureka

> Los canales `EMAIL`, `SMS` y `PUSH` están simulados para esta versión.

## Tecnologías

- Spring Boot 3.4.x
- Java 17
- Spring Data JPA
- H2 en memoria (perfil por defecto) o PostgreSQL con `SPRING_PROFILES_ACTIVE=postgres`
- Eureka Client
- Scheduler
- Flyway (solo con perfil `postgres`; tabla JPA `notificaciones`)

## Ejecutar el servicio

Desde la carpeta `ms-notificaciones`:

```bash
mvn spring-boot:run
```

El servicio arranca en:

- `http://localhost:8085`

## Endpoints principales

Base path: `/api/notificaciones`

### Notificaciones

- `POST /api/notificaciones`
- `GET /api/notificaciones/pendientes`
- `GET /api/notificaciones/{id}`
- `GET /api/notificaciones/paciente/{pacienteId}`
- `POST /api/notificaciones/{id}/enviar`
- `POST /api/notificaciones/{id}/enviar-canal?canal=EMAIL|SMS|PUSH`
- `POST /api/notificaciones/enviar-todas`

### Información

- `GET /api/notificaciones/info/canales`
- `GET /api/notificaciones/info/estado`

## Migraciones (Flyway, perfil `postgres`)

Con PostgreSQL/Insforge, Flyway usa la tabla de historial `flyway_ms_notificaciones`. La migración `V1` renombra la tabla legada `notifications` a **`notificaciones`** si aplica.

Configura el servidor Eureka en `src/main/resources/application.properties`:

```properties
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
```

## Pruebas

```bash
mvn test
```

## Estructura base

```text
src/main/java/com/saludrednorte/ms_notificaciones/
├── controller/
├── dto/
├── entity/
├── exception/
├── repository/
├── scheduler/
└── service/
```
