# ms-auth

Microservicio de autenticación y usuarios de RedNorte.

## Qué hace

- Gestión de usuarios y autenticación
- Generación y validación de tokens JWT
- Publicación de eventos de auditoría a RabbitMQ
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

Desde la carpeta `ms-auth`:

```bash
mvn spring-boot:run
```

El servicio arranca en:

- `http://localhost:8087`

## Endpoints principales

### Autenticación

- `POST /auth/login`: Autenticación de usuario y generación de token JWT
- `POST /auth/register`: Registro de nuevo usuario

### Usuarios

- `GET /auth/users`: Listar usuarios (requiere autenticación)
- `GET /auth/users/{id}`: Obtener usuario por ID (requiere autenticación)

### Información

- `GET /actuator/health`: Estado del servicio
- `GET /actuator/info`: Información del servicio

## Swagger

- `http://localhost:8087/swagger-ui.html`
- `http://localhost:8087/api-docs`

## Configuración

- Puerto: 8087
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
src/main/java/com/saludrednorte/ms_auth/
├── controller/
├── dto/
├── entity/
├── exception/
├── repository/
├── security/
├── service/
└── messaging/
```

