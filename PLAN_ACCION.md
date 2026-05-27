# 🚀 PLAN DE ACCIÓN - Integración Completa de Microservicios

**Documento:** Plan Ejecutable  
**Objetivo:** Conectar completamente los 3 microservicios + Implementar Insforge  
**Duración Estimada:** 6-7 horas  
**Fecha de Inicio:** 28 de Abril, 2026

---

## FASE 1: INTEGRACIÓN INMEDIATA (2-3 horas)

### Paso 1.1: Conectar ms-gestionpacientes → ms-notificaciones

**Objetivo:** Cuando se registra un paciente, automáticamente se crea una notificación

**Cambios en:** `ms-gestionpacientes/src/main/java/com/saludrednorte/ms_listas_espera/service/PacienteService.java`

```diff
@Service
public class PacienteService {

    @Autowired
    private PacienteRepository pacienteRepository;
    
+   @Autowired
+   private NotificationClient notificationClient;
    
    public Paciente registrarPaciente(Paciente paciente) {
        if (paciente.getDni() != null && pacienteRepository.existsByDniIgnoreCase(paciente.getDni())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un paciente con el DNI indicado");
        }
        
        Paciente savedPaciente = pacienteRepository.save(paciente);
        
+       // Crear notificación automáticamente
+       try {
+           NotificationRequestDTO notif = new NotificationRequestDTO();
+           notif.setPacienteId(savedPaciente.getId());
+           notif.setTipo("PACIENTE_REGISTRADO");
+           notif.setMensaje("Paciente " + savedPaciente.getNombre() + " " + 
+                             savedPaciente.getApellido() + " registrado exitosamente en el sistema");
+           notif.setCanal("EMAIL");
+           notificationClient.createNotification(notif);
+           logger.info("Notificación creada para paciente {}", savedPaciente.getId());
+       } catch (Exception e) {
+           // Log pero no fallar el registro
+           logger.warn("No se pudo crear notificación para paciente {}", 
+                       savedPaciente.getId(), e);
+       }
        
        return savedPaciente;
    }
    
    public Paciente actualizarPaciente(Paciente paciente) {
        if (paciente.getId() == null || !pacienteRepository.existsById(paciente.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Paciente no encontrado");
        }
        
        Paciente updatedPaciente = pacienteRepository.save(paciente);
        
+       // Notificar actualización
+       try {
+           NotificationRequestDTO notif = new NotificationRequestDTO();
+           notif.setPacienteId(updatedPaciente.getId());
+           notif.setTipo("PACIENTE_ACTUALIZADO");
+           notif.setMensaje("Datos del paciente actualizados");
+           notif.setCanal("EMAIL");
+           notificationClient.createNotification(notif);
+       } catch (Exception e) {
+           logger.warn("No se pudo crear notificación de actualización", e);
+       }
        
        return updatedPaciente;
    }
}
```

**Archivos a crear/verificar:**
- [ ] `ms-gestionpacientes/src/main/java/com/saludrednorte/ms_listas_espera/dto/NotificationRequestDTO.java`
  - Debe tener campos: `pacienteId`, `tipo`, `mensaje`, `canal`

---

### Paso 1.2: Conectar ms-optimizacion → ms-notificaciones

**Objetivo:** Cuando se asigna una cita, se notifica automáticamente al paciente

**Cambios en:** 

A) Crear nuevo cliente Feign:
`ms-optimizacion/src/main/java/com/saludrednorte/ms_optimizacion/client/NotificationClient.java`

```java
package com.saludrednorte.ms_optimizacion.client;

import com.saludrednorte.ms_optimizacion.dto.NotificationRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "ms-notificaciones")
public interface NotificationClient {
    
    @PostMapping("/api/notifications")
    ResponseEntity<Void> createNotification(@RequestBody NotificationRequestDTO request);
}
```

B) Actualizar `OptimizacionService.java`:

```diff
@Service
public class OptimizacionService {

    @Autowired
    private OptimizacionFactory factory;

    @Autowired
    private CitaService citaService;

    @Autowired
    private ListaEsperaClient listaEsperaClient;
    
+   @Autowired
+   private NotificationClient notificationClient;

    public void procesarCancelacion(Long citaId, String estrategiaTipo) {
        citaService.cancelarCita(citaId);
        Cita citaCancelada = citaService.obtenerCitaPorId(citaId).orElse(null);
        if (citaCancelada != null) {
            EstrategiaOptimizacion estrategia = factory.getEstrategia(estrategiaTipo);
            Cita citaReasignada = estrategia.reasignarCita(citaCancelada);
            
+           // Notificar reasignación
+           try {
+               NotificationRequestDTO notif = new NotificationRequestDTO();
+               notif.setPacienteId(citaReasignada.getPacienteId());
+               notif.setTipo("CITA_REASIGNADA");
+               notif.setMensaje("Su cita ha sido reasignada para " + 
+                               citaReasignada.getFecha() + " con " + 
+                               citaReasignada.getMedico().getNombre());
+               notif.setCanal("SMS");
+               notificationClient.createNotification(notif);
+               logger.info("Notificación de reasignación enviada");
+           } catch (Exception e) {
+               logger.warn("No se pudo notificar reasignación", e);
+           }
        }
    }

    @CircuitBreaker(name = "listaEsperaService", fallbackMethod = "fallbackListaEspera")
    public List<ListaEsperaDTO> obtenerListaEspera() {
        return listaEsperaClient.getListaEspera();
    }

    public List<ListaEsperaDTO> fallbackListaEspera(Throwable t) {
        return List.of();
    }
}
```

---

### Paso 1.3: Verificar DTOs en todos los servicios

**Archivo:** Verificar/crear `NotificationRequestDTO.java` en todos los servicios

**Estructura mínima:**
```java
package com.saludrednorte.ms_[servicio].dto;

public class NotificationRequestDTO {
    private Long pacienteId;
    private String tipo;           // "PACIENTE_REGISTRADO", "CITA_ASIGNADA", etc.
    private String mensaje;
    private String canal;          // "EMAIL", "SMS", "PUSH"
    
    // Getters y Setters
}
```

**Ubicaciones esperadas:**
- `ms-gestionpacientes/src/main/java/com/saludrednorte/ms_listas_espera/dto/NotificationRequestDTO.java` (ya existe)
- `ms-optimizacion/src/main/java/com/saludrednorte/ms_optimizacion/dto/NotificationRequestDTO.java` (crear)

---

## FASE 2: SINCRONIZAR VERSIONES (1-2 horas)

### Paso 2.1: Actualizar ms-notificaciones a Spring Boot 4.0.4

**Archivo:** `ms-notificaciones/pom.xml`

```diff
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
-   <version>2.7.12</version>
+   <version>4.0.4</version>
    <relativePath/>
</parent>

<properties>
-   <java.version>11</java.version>
+   <java.version>17</java.version>
-   <spring-cloud.version>2021.0.8</spring-cloud.version>
+   <spring-cloud.version>2025.1.1</spring-cloud.version>
</properties>

<dependencies>
    <!-- ... otras dependencias ... -->
    <dependency>
-       <version>1.7.0</version>
+       <version>2.8.13</version>
    </dependency>
</dependencies>
```

**Luego ejecutar:**
```powershell
cd ms-notificaciones
./mvnw clean install
```

---

## FASE 3: IMPLEMENTAR INSFORGE (2-3 horas)

**Prerequisito:** Necesito que me confirmes:
1. ¿Qué es exactamente Insforge? (¿PostgreSQL, MySQL, BD propietaria?)
2. ¿Tienes credenciales de acceso?
3. ¿URL/host de conexión?

**Asumiendo que Insforge es compatible con PostgreSQL:**

### Paso 3.1: Configurar application-postgres.yml

**Archivo (crear/actualizar en cada servicio):** `application-postgres.yml`

```yaml
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:saludrednorte}
    driverClassName: org.postgresql.Driver
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:password}
  
  jpa:
    database-platform: org.hibernate.dialect.PostgreSQL10Dialect
    hibernate:
      ddl-auto: ${JPA_DDL_AUTO:update}
    show-sql: false
```

### Paso 3.2: Actualizar application.yml (agregar perfil)

```diff
spring:
  application:
    name: ms-gestionpacientes
+  profiles:
+    active: ${SPRING_PROFILES_ACTIVE:postgres}
```

### Paso 3.3: Configurar docker-compose.yml

```yaml
version: '3.8'

services:
  insforge-db:
    image: postgres:15-alpine
    container_name: saludrednorte-db
    environment:
      POSTGRES_DB: saludrednorte
      POSTGRES_USER: ${DB_USERNAME}
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  # Microservicios...
  eureka-server:
    # ...

volumes:
  postgres_data:
```

### Paso 3.4: Variables de Entorno

**Crear `.env` en raíz del proyecto:**
```env
DB_HOST=localhost
DB_PORT=5432
DB_NAME=saludrednorte
DB_USERNAME=postgres
DB_PASSWORD=tu_password_segura
DB_DRIVER=org.postgresql.Driver
SPRING_PROFILES_ACTIVE=postgres
```

---

## FASE 4: TESTING E2E (1 hora)

### Paso 4.1: Ejecutar Smoke Tests

```powershell
cd C:\Users\ibane\Desktop\Fullstack
.\scripts\smoke-test-e2e.ps1
```

### Paso 4.2: Test Manual del Flujo

1. **Registrar Paciente:**
```bash
curl -X POST http://localhost:8080/pacientes \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Juan",
    "apellido": "Pérez",
    "dni": "12345678",
    "email": "juan@example.com"
  }'
```

2. **Verificar Notificación Creada:**
```bash
curl -X GET http://localhost:8080/api/notifications/pending
```

3. **Verificar Lista de Espera:**
```bash
curl -X GET http://localhost:8080/lista-espera
```

4. **Consultar en Optimización:**
```bash
curl -X GET http://localhost:8080/optimizacion/lista-espera
```

---

## FASE 5: DOCUMENTACIÓN (30 min)

### Paso 5.1: Actualizar ARCHITECTURE.md

Agregar nueva sección:

```markdown
## Flujo de Trabajo E2E

1. **Paciente registrado** en ms-gestionpacientes
   - Evento: `PACIENTE_REGISTRADO`
   - Notificación: EMAIL

2. **Sistema crea notificación** en ms-notificaciones
   - Estado: `PENDIENTE` → `ENVIADA`

3. **Optimización consulta disponibilidad** en ms-gestionpacientes
   - Endpoint: `GET /lista-espera`
   - Circuit Breaker: Habilitado

4. **Cita asignada** en ms-optimizacion
   - Estrategia: FIFO, POR_GRAVEDAD, o OPTIMIZADA
   - Evento: `CITA_ASIGNADA`
   - Notificación: SMS o PUSH

5. **Paciente notificado** en ms-notificaciones
   - Estado: `ENVIADA`
```

---

## CHECKLIST FINAL

### Fase 1: Integraciones
- [ ] Inyectar NotificationClient en PacienteService
- [ ] Llamar a createNotification en registrarPaciente
- [ ] Llamar a createNotification en actualizarPaciente
- [ ] Crear NotificationClient en ms-optimizacion
- [ ] Inyectar NotificationClient en OptimizacionService
- [ ] Llamar a createNotification en procesarCancelacion
- [ ] Crear NotificationRequestDTO en ms-optimizacion

### Fase 2: Versionado
- [ ] Actualizar Spring Boot 4.0.4 en ms-notificaciones
- [ ] Actualizar Java 17 en ms-notificaciones
- [ ] Actualizar spring-cloud 2025.1.1 en ms-notificaciones
- [ ] Ejecutar `mvnw clean install` en ms-notificaciones
- [ ] Verificar compilación exitosa

### Fase 3: Insforge
- [ ] Obtener credenciales de Insforge
- [ ] Crear application-postgres.yml en 3 servicios
- [ ] Actualizar pom.xml con driver PostgreSQL (si aplica)
- [ ] Configurar docker-compose.yml
- [ ] Crear .env con credenciales
- [ ] Ejecutar migraciones de BD

### Fase 4: Testing
- [ ] Ejecutar smoke-test-e2e.ps1
- [ ] Test manual: Registrar paciente
- [ ] Test manual: Verificar notificación
- [ ] Test manual: Consultar lista de espera
- [ ] Test manual: Asignar cita
- [ ] Verificar logging en consola

### Fase 5: Documentación
- [ ] Actualizar ARCHITECTURE.md
- [ ] Actualizar ANALISIS_MICROSERVICIOS.md
- [ ] Crear PATTERNS.md (template para futuros MS)

---

## Orden de Ejecución Recomendado

```
LUNES (4 horas):
1. Fase 1.1 - PacienteService (30 min)
2. Fase 1.2 - OptimizacionService (45 min)
3. Fase 1.3 - Verificar DTOs (15 min)
4. Testing básico (45 min)

MARTES (3-4 horas):
1. Fase 2 - Sincronizar Spring Boot (1.5 h)
2. Fase 3 - Configurar Insforge (1.5-2 h)
3. Fase 4 - Testing E2E (1 h)
4. Fase 5 - Documentación (30 min)
```

---

**Status:** Listo para ejecutar. Confirma sobre Insforge para proceder con Fase 3.

