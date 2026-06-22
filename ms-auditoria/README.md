# ms-auditoria

Microservicio de auditoría de eventos de RedNorte.

## Qué hace

- Recibe y almacena eventos de auditoría desde otros microservicios
- Consume eventos de RabbitMQ
- Consulta de eventos de auditoría por usuario, tipo de acción, o fecha
- Endpoints protegidos con Spring Security

## Tecnologías

- Spring Boot 3.4.1
- Java 17
- Spring Security
- JWT (jjwt 0.11.5)
- Spring Data JPA
- H2 en memoria (perfil por defecto) o PostgreSQL con `SPRING_PROFILES_ACTIVE=postgres`
- Eureka Client
- RabbitMQ
- SpringDoc OpenAPI (Swagger)

## Ejecutar el servicio

Desde la carpeta `ms-auditoria`:

```bash
mvn spring-boot:run
```

El servicio arranca en:

- `http://localhost:8088`

## Endpoints principales

### Auditoría

- `GET /auditoria/eventos`: Listar todos los eventos de auditoría (requiere autenticación)
- `GET /auditoria/eventos/{id}`: Obtener evento por ID (requiere autenticación)
- `GET /auditoria/eventos/usuario/{usuarioId}`: Obtener eventos por usuario (requiere autenticación)
- `GET /auditoria/eventos/tipo/{tipoAccion}`: Obtener eventos por tipo de acción (requiere autenticación)

### Información

- `GET /actuator/health`: Estado del servicio
- `GET /actuator/info`: Información del servicio

## Swagger

- `http://localhost:8088/swagger-ui.html`
- `http://localhost:8088/api-docs`

## Configuración

- Puerto: 8088
- Base de datos: H2 en memoria (por defecto) o PostgreSQL
- Eureka: Registrado en eureka-server:8761
- RabbitMQ: localhost:5672 (configurable por variables de entorno)

## Variables de entorno

- `RABBITMQ_HOST`: Host de RabbitMQ (default: localhost)
- `RABBITMQ_PORT`: Puerto de RabbitMQ (default: 5672)
- `RABBITMQ_USERNAME`: Usuario de RabbitMQ (default: guest)
- `RABBITMQ_PASSWORD`: Password de RabbitMQ (default: guest)

## Pruebas

```bash
mvn test
```

## Estructura base

```text
src/main/java/com/saludrednorte/ms_auditoria/
├── controller/
├── dto/
├── entity/
├── exception/
├── repository/
├── security/
├── service/
└── messaging/
```

