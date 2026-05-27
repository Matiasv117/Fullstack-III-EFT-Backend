# 📐 PATTERNS.md - Template para Nuevos Microservicios

**Objetivo:** Asegurar que todos los futuros microservicios sigan el mismo patrón de integración

---

## Checklist de Versionado

Todo nuevo microservicio DEBE tener:

```xml
<!-- pom.xml -->
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>4.0.4</version>
</parent>

<properties>
    <java.version>17</java.version>
    <spring-cloud.version>2025.1.1</spring-cloud.version>
</properties>
```

---

## Checklist de Dependencias

### Siempre incluir:

```xml
<!-- Service Discovery & Communication -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>

<!-- Database -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>

<!-- Validation & Testing -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>

<!-- Observability -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>

<!-- Documentation -->
<dependency>
    <version>2.8.13</version>
</dependency>
```

### Si requiere resiliencia (recomendado):

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-circuitbreaker-resilience4j</artifactId>
</dependency>
```

### Si requiere scheduling:

```xml
<!-- En @SpringBootApplication -->
@EnableScheduling
```

---

## Estructura de Directorios Estándar

```
ms-[nombre]/
├── src/
│   ├── main/
│   │   ├── java/com/saludrednorte/ms_[nombre]/
│   │   │   ├── Ms[Nombre]Application.java          # Clase principal
│   │   │   ├── client/                              # Clientes Feign
│   │   │   │   ├── OtroServicioClient.java
│   │   │   │   └── MasServiciosClient.java
│   │   │   ├── controller/                          # REST Controllers
│   │   │   │   ├── [Entidad]Controller.java
│   │   │   │   └── ...
│   │   │   ├── service/                             # Lógica de negocio
│   │   │   │   ├── [Entidad]Service.java
│   │   │   │   └── ...
│   │   │   ├── repository/                          # JPA Repositories
│   │   │   │   ├── [Entidad]Repository.java
│   │   │   │   └── ...
│   │   │   ├── entity/                              # Entidades JPA
│   │   │   │   ├── [Entidad].java
│   │   │   │   ├── enums/
│   │   │   │   │   └── [Enum].java
│   │   │   │   └── ...
│   │   │   ├── dto/                                 # Data Transfer Objects
│   │   │   │   ├── [Entidad]RequestDTO.java
│   │   │   │   ├── [Entidad]ResponseDTO.java
│   │   │   │   └── ...
│   │   │   ├── exception/                           # Custom Exceptions
│   │   │   │   └── [Nombre]Exception.java
│   │   │   └── util/                                # Utilidades
│   │   │       └── [Utilidad].java
│   │   └── resources/
│   │       ├── application.yml                      # Config por defecto
│   │       ├── application-postgres.yml             # Config Insforge
│   │       └── db/migration/                        # Migraciones (si Flyway)
│   │           └── V1__initial_schema.sql
│   └── test/
│       ├── java/com/saludrednorte/ms_[nombre]/
│       │   ├── [Entidad]ServiceTest.java
│       │   ├── [Entidad]ControllerTest.java
│       │   └── ...
│       └── resources/
│           └── application-test.yml
├── pom.xml
├── Dockerfile
└── README.md
```

---

## Configuración application.yml Estándar

```yaml
server:
  port: ${SERVER_PORT:8090}

spring:
  application:
    name: ms-[nombre]
  
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:postgres}
  
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/saludrednorte}
    driverClassName: ${DB_DRIVER:org.postgresql.Driver}
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:password}
  
  jpa:
    database-platform: org.hibernate.dialect.PostgreSQL10Dialect
    hibernate:
      ddl-auto: ${JPA_DDL_AUTO:update}
    show-sql: ${JPA_SHOW_SQL:false}

  cloud:
    openfeign:
      client:
        config:
          default:
            connectTimeout: ${FEIGN_CONNECT_TIMEOUT:2000}
            readTimeout: ${FEIGN_READ_TIMEOUT:5000}

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
    register-with-eureka: true
    fetch-registry: true
  
  instance:
    prefer-ip-address: true

resilience4j:
  circuitbreaker:
    instances:
      [nombreServicio]:
        failureRateThreshold: 50
        waitDurationInOpenState: 10000
        permittedNumberOfCallsInHalfOpenState: 3
        slidingWindowSize: 10

management:
  endpoints:
    web:
      exposure:
        include: health,info
  
  endpoint:
    health:
      probes:
        enabled: true
  
  health:
    livenessstate:
      enabled: true
    readinessstate:
      enabled: true


logging:
  level:
    root: WARN
    org.springframework: INFO
    com.saludrednorte: DEBUG
```

---

## Clase Principal Estándar

```java
package com.saludrednorte.ms_[nombre];

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling; // Si necesita scheduler

/**
 * Aplicación principal del Microservicio de [Nombre Descriptivo]
 * 
 * Responsabilidades:
 * - [Descripción de qué hace]
 * 
 * Integraciones:
 * - Eureka: Service Discovery
 * - OpenFeign: Comunicación con otros servicios
 * - [Otros servicios específicos]
 */
@SpringBootApplication
@EnableFeignClients
@EnableDiscoveryClient
// @EnableScheduling  // Descomentar si necesita scheduler
public class Ms[Nombre]Application {

    public static void main(String[] args) {
        SpringApplication.run(Ms[Nombre]Application.class, args);
    }
}
```

---

## Cliente Feign Estándar

```java
package com.saludrednorte.ms_[nombre].client;

import com.saludrednorte.ms_[nombre].dto.OtroServicioDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * Cliente Feign para comunicación con [NombreOtroServicio]
 * 
 * Características:
 * - Circuit Breaker habilitado
 * - Timeout configurado en application.yml
 * - Fallback automático si el servicio está caído
 */
@FeignClient(name = "ms-[otro-servicio]")
@CircuitBreaker(name = "[nombreServicio]")
public interface [OtroServicio]Client {

    @GetMapping("/api/endpoint")
    OtroServicioDTO obtenerDatos();

    @PostMapping("/api/endpoint")
    OtroServicioDTO crearDatos(@RequestBody OtroServicioDTO dto);

    // Implementar fallback:
    // En el Service que inyecta este cliente, crear método:
    // public OtroServicioDTO fallbackObtenerDatos(Throwable t) { ... }
}
```

---

## Service Estándar con Integración

```java
package com.saludrednorte.ms_[nombre].service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@Service
public class [Entidad]Service {

    private static final Logger logger = LoggerFactory.getLogger([Entidad]Service.class);

    @Autowired
    private [Entidad]Repository repository;

    @Autowired
    private [OtroServicio]Client otroServicioClient;

    @Transactional
    public [Entidad] crear([Entidad] entity) {
        logger.info("Creando [entidad]: {}", entity);
        
        // Validación
        if (entity == null) {
            throw new IllegalArgumentException("[Entidad] no puede ser nula");
        }

        // Llamar a otro servicio con manejo de error
        try {
            OtroServicioDTO datos = otroServicioClient.obtenerDatos();
            // Usar datos...
        } catch (Exception e) {
            logger.warn("No se pudo obtener datos de OtroServicio", e);
            // Continuar con fallback o lanzar excepción
        }

        // Guardar
        return repository.save(entity);
    }

    @CircuitBreaker(name = "[nombreServicio]", fallbackMethod = "fallbackListar")
    public List<[Entidad]> listar() {
        return repository.findAll();
    }

    public List<[Entidad]> fallbackListar(Throwable t) {
        logger.error("Circuit breaker abierto. Retornando lista vacía", t);
        return List.of();
    }

    public void eliminar(Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "[Entidad] no encontrada");
        }
        repository.deleteById(id);
    }
}
```

---

## Controller Estándar

```java
package com.saludrednorte.ms_[nombre].controller;

import com.saludrednorte.ms_[nombre].dto.[Entidad]RequestDTO;
import com.saludrednorte.ms_[nombre].dto.[Entidad]ResponseDTO;
import com.saludrednorte.ms_[nombre].service.[Entidad]Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller para [Entidad]
 * Base URL: /[entidades]
 */
@RestController
@RequestMapping("/[entidades]")
public class [Entidad]Controller {

    @Autowired
    private [Entidad]Service service;

    @PostMapping
    public ResponseEntity<[Entidad]ResponseDTO> crear(@Valid @RequestBody [Entidad]RequestDTO request) {
        // Mapear DTO → Entity
        // Llamar service
        // Mapear Entity → ResponseDTO
        return ResponseEntity.ok(/* response */);
    }

    @GetMapping
    public ResponseEntity<List<[Entidad]ResponseDTO>> listar() {
        // Implementar paginación si necesario
        return ResponseEntity.ok(/* responses */);
    }

    @GetMapping("/{id}")
    public ResponseEntity<[Entidad]ResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(/* response */);
    }

    @PutMapping("/{id}")
    public ResponseEntity<[Entidad]ResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody [Entidad]RequestDTO request) {
        return ResponseEntity.ok(/* response */);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
```

---

## Entity Estándar

```java
package com.saludrednorte.ms_[nombre].entity;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad [Nombre]
 * 
 * Tabla: [nombres_tabla]
 * Descripción: [Qué representa esta entidad]
 */
@Entity
@Table(name = "[nombres_tabla]")
public class [Entidad] {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "[columna]", nullable = false, unique = true)
    private String campo;

    @Column(name = "creado_at", nullable = false, updatable = false)
    private LocalDateTime creadoAt;

    @Column(name = "actualizado_at")
    private LocalDateTime actualizadoAt;

    @PrePersist
    protected void onCreate() {
        creadoAt = LocalDateTime.now();
        actualizadoAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        actualizadoAt = LocalDateTime.now();
    }

    // Getters y Setters
}
```

---

## DTO Estándar

```java
package com.saludrednorte.ms_[nombre].dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class [Entidad]RequestDTO {

    @NotBlank(message = "Campo no puede estar vacío")
    private String campo;

    @NotNull(message = "Campo es requerido")
    private Long otroId;
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class [Entidad]ResponseDTO {

    private Long id;
    private String campo;
    private Long otroId;
}
```

---

## Dockerfile Estándar

```dockerfile
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Copiar archivo compilado
COPY target/ms-[nombre]-*.jar app.jar

# Exponer puerto
EXPOSE ${SERVER_PORT:-8090}

# Healthcheck
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:${SERVER_PORT:-8090}/actuator/health || exit 1

# Comando de inicio
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## Variables de Entorno Estándar (.env)

```env
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=saludrednorte
DB_USERNAME=postgres
DB_PASSWORD=tu_password_segura
DB_DRIVER=org.postgresql.Driver
DB_URL=jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}

# Spring
SPRING_PROFILES_ACTIVE=postgres
JPA_DDL_AUTO=update
JPA_SHOW_SQL=false

# Feign
FEIGN_CONNECT_TIMEOUT=2000
FEIGN_READ_TIMEOUT=5000

# Server
SERVER_PORT=8090

# Eureka
EUREKA_URL=http://localhost:8761/eureka/
```

---

## Testing Unitario Estándar

```java
package com.saludrednorte.ms_[nombre];

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class [Entidad]ServiceTest {

    @Mock
    private [Entidad]Repository repository;

    @InjectMocks
    private [Entidad]Service service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCrear() {
        // Arrange
        [Entidad] entity = new [Entidad]();
        when(repository.save(entity)).thenReturn(entity);

        // Act
        [Entidad] result = service.crear(entity);

        // Assert
        assertNotNull(result);
        verify(repository, times(1)).save(entity);
    }
}
```

---

## README.md Estándar

```markdown
# Microservicio [Nombre]

## Descripción
Breve descripción de qué hace este microservicio.

## Puerto
Puerto 8090 (o el especificado)

## Dependencias
- Spring Boot 4.0.4
- PostgreSQL (Insforge)
- Eureka
- OpenFeign

## Configuración

### Variables de Entorno
```env
DB_URL=jdbc:postgresql://localhost:5432/saludrednorte
DB_USERNAME=postgres
DB_PASSWORD=password
SERVER_PORT=8090
```

### Ejecutar Localmente
```bash
mvn clean spring-boot:run
```

### Docker
```bash
docker build -t ms-[nombre] .
docker run -p 8090:8090 --env-file .env ms-[nombre]
```

## Endpoints Principales
- `POST /[entidades]` - Crear
- `GET /[entidades]` - Listar
- `GET /[entidades]/{id}` - Obtener por ID
- `PUT /[entidades]/{id}` - Actualizar
- `DELETE /[entidades]/{id}` - Eliminar

## Swagger

## Tests
```bash
mvn test
```
```

---

**Conclusión:** Usar este template asegura consistencia en todos los microservicios futuros.

