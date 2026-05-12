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

## Ejecutar el servicio

Desde la carpeta `ms-notificaciones`:

```bash
mvn spring-boot:run
```

El servicio arranca en:

- `http://localhost:8085`

## Endpoints principales

Base path: `/api/notifications`

### Notificaciones

- `POST /api/notifications`
- `GET /api/notifications/pending`
- `GET /api/notifications/{id}`
- `GET /api/notifications/paciente/{pacienteId}`
- `POST /api/notifications/{id}/send`
- `POST /api/notifications/{id}/send-by-channel?canal=EMAIL|SMS|PUSH`
- `POST /api/notifications/send-all`

### Información

- `GET /api/notifications/info/channels`
- `GET /api/notifications/info/health`

## Eureka

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
