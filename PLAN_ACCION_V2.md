# 🚀 PLAN DE ACCIÓN ACTUALIZADO - Con Insforge BaaS

**Versión:** 2.0 - Ajustada para Insforge como Backend-as-a-Service  
**Fecha:** 28 de Abril, 2026  
**Objetivo:** Integración completa + Insforge como BD persistente  

---

## 🔄 CAMBIO IMPORTANTE

Insforge NO es PostgreSQL tradicional. Es un **Backend-as-a-Service diseñado para IA**.

**Implicación:** La Fase 3 es completamente diferente a lo que planifiqué.

---

## ✅ PLAN FINAL (6-7 horas)

### FASE 1: INTEGRACIÓN DE MICROSERVICIOS (2-3 horas)
**Objetivo:** Conectar los 3 servicios entre sí  
**Dificultad:** Baja  
**Riesgo:** Muy bajo  

### FASE 2: SINCRONIZAR SPRING BOOT (1-2 horas)
**Objetivo:** Versiones consistentes  
**Dificultad:** Baja  
**Riesgo:** Muy bajo  

### FASE 3: INSFORGE COMO BD (2-3 horas)
**Objetivo:** BD persistente usando Insforge BaaS  
**Dificultad:** Media (documentación es nueva)  
**Riesgo:** Bajo (tienes IA disponible)  

### FASE 4: TESTING E2E (1 hora)
**Objetivo:** Verificar todo funciona  
**Dificultad:** Baja  

### FASE 5: DOCUMENTACIÓN (30 min)
**Objetivo:** Actualizar documentos  
**Dificultad:** Muy baja  

---

## 📋 FASE 1: INTEGRACIÓN INMEDIATA (2-3 HORAS)

### PASO 1.1: Conectar ms-gestionpacientes → ms-notificaciones
**Tiempo:** 30 minutos

**Archivo a modificar:**
`ms-gestionpacientes/src/main/java/com/saludrednorte/ms_listas_espera/service/PacienteService.java`

**Qué hacer:**
1. Agregar `@Autowired` para inyectar `NotificationClient`
2. En método `registrarPaciente()` → agregar llamada a notificaciones
3. En método `actualizarPaciente()` → agregar llamada a notificaciones

**Código específico:**

```java
package com.saludrednorte.ms_listas_espera.service;

import com.saludrednorte.ms_listas_espera.client.NotificationClient;
import com.saludrednorte.ms_listas_espera.dto.NotificationRequestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// ...existing imports...

@Service
public class PacienteService {

    private static final Logger logger = LoggerFactory.getLogger(PacienteService.class);

    @Autowired
    private PacienteRepository pacienteRepository;
    
    // AGREGAR ESTO:
    @Autowired
    private NotificationClient notificationClient;

    public Paciente registrarPaciente(Paciente paciente) {
        if (paciente.getDni() != null && pacienteRepository.existsByDniIgnoreCase(paciente.getDni())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un paciente con el DNI indicado");
        }
        
        Paciente savedPaciente = pacienteRepository.save(paciente);
        
        // AGREGAR ESTO:
        try {
            NotificationRequestDTO notif = new NotificationRequestDTO();
            notif.setPacienteId(savedPaciente.getId());
            notif.setTipo("PACIENTE_REGISTRADO");
            notif.setMensaje("Paciente " + savedPaciente.getNombre() + " " + 
                            savedPaciente.getApellido() + " registrado en el sistema");
            notif.setCanal("EMAIL");
            notificationClient.createNotification(notif);
            logger.info("Notificación creada para paciente {}", savedPaciente.getId());
        } catch (Exception e) {
            logger.warn("Fallo en notificación pero paciente registrado: {}", e.getMessage());
        }
        
        return savedPaciente;
    }

    public Paciente actualizarPaciente(Paciente paciente) {
        if (paciente.getId() == null || !pacienteRepository.existsById(paciente.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Paciente no encontrado");
        }
        
        Paciente updatedPaciente = pacienteRepository.save(paciente);
        
        // AGREGAR ESTO:
        try {
            NotificationRequestDTO notif = new NotificationRequestDTO();
            notif.setPacienteId(updatedPaciente.getId());
            notif.setTipo("PACIENTE_ACTUALIZADO");
            notif.setMensaje("Datos del paciente actualizados");
            notif.setCanal("EMAIL");
            notificationClient.createNotification(notif);
        } catch (Exception e) {
            logger.warn("Fallo en notificación de actualización: {}", e.getMessage());
        }
        
        return updatedPaciente;
    }

    // ...resto del código sin cambios...
}
```

**Checklist:**
- [ ] Código pegado
- [ ] Compilar sin errores
- [ ] Verificar que no hay imports faltantes

---

### PASO 1.2: Conectar ms-optimizacion → ms-notificaciones
**Tiempo:** 45 minutos

**Archivos a crear/modificar:**

#### A) Crear archivo nuevo:
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

#### B) Crear/verificar DTO:
`ms-optimizacion/src/main/java/com/saludrednorte/ms_optimizacion/dto/NotificationRequestDTO.java`

```java
package com.saludrednorte.ms_optimizacion.dto;

public class NotificationRequestDTO {
    private Long pacienteId;
    private String tipo;
    private String mensaje;
    private String canal;
    
    // Constructor vacío
    public NotificationRequestDTO() {}
    
    // Getters y Setters
    public Long getPacienteId() {
        return pacienteId;
    }
    
    public void setPacienteId(Long pacienteId) {
        this.pacienteId = pacienteId;
    }
    
    public String getTipo() {
        return tipo;
    }
    
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
    
    public String getMensaje() {
        return mensaje;
    }
    
    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
    
    public String getCanal() {
        return canal;
    }
    
    public void setCanal(String canal) {
        this.canal = canal;
    }
}
```

#### C) Modificar OptimizacionService:
`ms-optimizacion/src/main/java/com/saludrednorte/ms_optimizacion/service/OptimizacionService.java`

```java
package com.saludrednorte.ms_optimizacion.service;

import com.saludrednorte.ms_optimizacion.client.ListaEsperaClient;
import com.saludrednorte.ms_optimizacion.client.NotificationClient;
import com.saludrednorte.ms_optimizacion.dto.ListaEsperaDTO;
import com.saludrednorte.ms_optimizacion.dto.NotificationRequestDTO;
import com.saludrednorte.ms_optimizacion.entity.Cita;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OptimizacionService {

    private static final Logger logger = LoggerFactory.getLogger(OptimizacionService.class);

    @Autowired
    private OptimizacionFactory factory;

    @Autowired
    private CitaService citaService;

    @Autowired
    private ListaEsperaClient listaEsperaClient;
    
    // AGREGAR ESTO:
    @Autowired
    private NotificationClient notificationClient;

    public void procesarCancelacion(Long citaId, String estrategiaTipo) {
        citaService.cancelarCita(citaId);
        Cita citaCancelada = citaService.obtenerCitaPorId(citaId).orElse(null);
        if (citaCancelada != null) {
            EstrategiaOptimizacion estrategia = factory.getEstrategia(estrategiaTipo);
            Cita citaReasignada = estrategia.reasignarCita(citaCancelada);
            
            // AGREGAR ESTO:
            try {
                NotificationRequestDTO notif = new NotificationRequestDTO();
                notif.setPacienteId(citaReasignada.getPacienteId());
                notif.setTipo("CITA_REASIGNADA");
                notif.setMensaje("Cita reasignada para " + citaReasignada.getFecha());
                notif.setCanal("SMS");
                notificationClient.createNotification(notif);
                logger.info("Notificación de reasignación enviada");
            } catch (Exception e) {
                logger.warn("Fallo al notificar reasignación: {}", e.getMessage());
            }
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

**Checklist:**
- [ ] Crear NotificationClient.java
- [ ] Crear NotificationRequestDTO.java
- [ ] Actualizar OptimizacionService.java
- [ ] Compilar sin errores

---

### PASO 1.3: Verificar DTOs están en ambos servicios
**Tiempo:** 15 minutos

**Verificar que existen:**
- [ ] `ms-gestionpacientes/src/.../dto/NotificationRequestDTO.java`
- [ ] `ms-optimizacion/src/.../dto/NotificationRequestDTO.java`

Ambos deben tener los mismos campos:
```java
- Long pacienteId
- String tipo
- String mensaje
- String canal
```

**Si faltan crear:**
Copiar el mismo código mostrado arriba en ambas ubicaciones.

---

### Testing FASE 1
**Tiempo:** 15 minutos

Ejecutar en terminal (en cada carpeta):
```powershell
cd ms-gestionpacientes
mvnw clean compile
# Debe completar sin errores

cd ..\ms-optimizacion
mvnw clean compile
# Debe completar sin errores

cd ..\ms-notificaciones
mvnw clean compile
# Debe completar sin errores
```

**Si hay errores:**
1. Verificar imports
2. Verificar nombres de clases
3. Verificar paquetes
4. Pedir ayuda a IA

---

## 📋 FASE 2: SINCRONIZAR SPRING BOOT (1-2 HORAS)

### PASO 2.1: Actualizar ms-notificaciones
**Tiempo:** 1-2 horas (incluye compilación)

**Archivo a modificar:**
`ms-notificaciones/pom.xml`

**Cambios específicos:**

```xml
<!-- LÍNEA 8 - Cambiar parent version -->
ANTES: <version>2.7.12</version>
DESPUÉS: <version>4.0.4</version>

<!-- LÍNEA 18 - Cambiar Java version -->
ANTES: <java.version>11</java.version>
DESPUÉS: <java.version>17</java.version>

<!-- LÍNEA 19 - Cambiar spring-cloud version -->
ANTES: <spring-cloud.version>2021.0.8</spring-cloud.version>
DESPUÉS: <spring-cloud.version>2025.1.1</spring-cloud.version>

ANTES: <version>1.7.0</version>
DESPUÉS: <version>2.8.13</version>

<!-- REEMPLAZAR NOMBRE DEL ARTIFACT TAMBIÉN:
-->
DESPUÉS:
```

**Pasos:**
1. Abre el archivo pom.xml
2. Busca cada línea indicada
3. Realiza el cambio
4. Guarda el archivo

**Compilar:**
```powershell
cd ms-notificaciones
mvnw clean install
# Esto tardará 2-3 minutos
```

**Si hay errores:**
- Verificar que los cambios sean exactos
- Limpiar caché: `mvnw clean`
- Intentar nuevamente

**Checklist:**
- [ ] Línea 8 cambiada (Spring Boot 4.0.4)
- [ ] Línea 18 cambiada (Java 17)
- [ ] Línea 19 cambiada (spring-cloud 2025.1.1)
- [ ] Compilación exitosa

---

## 📋 FASE 3: INSFORGE COMO BD (2-3 HORAS)

### ¿QUÉ ES INSFORGE?
Insforge es un Backend-as-a-Service (BaaS) diseñado para agentes de IA.

**Características:**
- API REST para CRUD
- Autenticación integrada
- Funciones backend
- Diseñado para ser usado por IA

### PASO 3.1: Obtener Insforge
**Tiempo:** 30 minutos

1. **Ir a Insforge** (necesitarás google qué es exactamente)
   - URL probable: https://insforge.io (o similar)
   - Crear cuenta
   - Crear proyecto

2. **Obtener credenciales:**
   - API Key
   - Project ID
   - Base URL de Insforge

3. **Guardar credenciales:**
   Crear archivo `.env` en raíz del proyecto:
   ```env
   INSFORGE_API_KEY=tu_api_key_aqui
   INSFORGE_PROJECT_ID=tu_project_id_aqui
   INSFORGE_API_URL=https://api.insforge.io/v1
   ```

**Checklist:**
- [ ] Cuenta creada en Insforge
- [ ] Proyecto creado
- [ ] Credenciales obtenidas
- [ ] Archivo .env creado con credenciales

---

### PASO 3.2: Crear entidades en Insforge
**Tiempo:** 45 minutos

Necesitas crear las tablas/modelos en Insforge para:
1. Pacientes
2. Notificaciones
3. Citas

**Opción A: Usando dashboard de Insforge**
- Ir a Insforge dashboard
- Crear modelo "Pacientes" con campos: id, nombre, apellido, dni, email
- Crear modelo "Notificaciones" con campos: id, pacienteId, tipo, mensaje, canal, estado
- Crear modelo "Citas" con campos: id, pacienteId, medicoId, fecha, estado

**Opción B: Usando IA (Recomendado)**
- Decirle a Cursor/Claude: "Crea las tablas en Insforge para [nombre del proyecto]"
- La IA puede generar las llamadas a API de Insforge

**Checklist:**
- [ ] Tabla/Modelo "Pacientes" creado en Insforge
- [ ] Tabla/Modelo "Notificaciones" creado en Insforge
- [ ] Tabla/Modelo "Citas" creado en Insforge

---

### PASO 3.3: Configurar cada microservicio para usar Insforge
**Tiempo:** 1 hora

Para cada servicio, necesitas:
1. Agregar dependencia de HTTP client (si no la tiene)
2. Crear servicio que llame a Insforge API
3. Reemplazar llamadas a BD local por llamadas a Insforge

**Para ms-gestionpacientes:**

A) Crear servicio que llama a Insforge:
```java
// Crear: ms-gestionpacientes/src/.../service/InsforgeService.java

package com.saludrednorte.ms_listas_espera.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@Service
public class InsforgeService {
    
    @Value("${insforge.api.url}")
    private String insforgeUrl;
    
    @Value("${insforge.api.key}")
    private String apiKey;
    
    private RestTemplate restTemplate = new RestTemplate();
    
    public Object createPaciente(Object paciente) {
        String url = insforgeUrl + "/collections/pacientes";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);
        
        HttpEntity<Object> request = new HttpEntity<>(paciente, headers);
        return restTemplate.postForObject(url, request, Object.class);
    }
    
    // Métodos similares para GET, PUT, DELETE
}
```

B) Actualizar application.yml:
```yaml
insforge:
  api:
    url: ${INSFORGE_API_URL:https://api.insforge.io/v1}
    key: ${INSFORGE_API_KEY}
    project-id: ${INSFORGE_PROJECT_ID}
```

C) Actualizar PacienteService para usar Insforge en lugar de BD local:
```java
// En PacienteService.java
@Autowired
private InsforgeService insforgeService;

public Paciente registrarPaciente(Paciente paciente) {
    // Cambiar de: pacienteRepository.save(paciente)
    // A: insforgeService.createPaciente(paciente)
    // Y recuperar resultado
    
    Object resultado = insforgeService.createPaciente(paciente);
    // Convertir resultado a Paciente...
}
```

**NOTA:** Esta es la parte más compleja. Si te cuesta, pide ayuda a IA.

**Checklist:**
- [ ] Crear InsforgeService en ms-gestionpacientes
- [ ] Crear InsforgeService en ms-notificaciones
- [ ] Crear InsforgeService en ms-optimizacion
- [ ] Actualizar application.yml en cada servicio
- [ ] Actualizar Services para usar InsforgeService

---

### PASO 3.4: Variables de entorno
**Tiempo:** 15 minutos

Crear `.env` en raíz del proyecto:
```env
# Insforge
INSFORGE_API_URL=https://api.insforge.io/v1
INSFORGE_API_KEY=tu_api_key_aqui
INSFORGE_PROJECT_ID=tu_project_id_aqui

# Spring
SPRING_PROFILES_ACTIVE=default

# Database (ya no necesarias pero puedes dejar para testing)
DB_URL=jdbc:h2:mem:testdb
DB_DRIVER=org.h2.Driver
DB_USERNAME=sa
DB_PASSWORD=password
```

**Checklist:**
- [ ] Archivo .env creado
- [ ] Variables correctas
- [ ] NO compartir .env en git (agregar a .gitignore)

---

## 📋 FASE 4: TESTING E2E (1 HORA)

### PASO 4.1: Compilar todo
```powershell
# En cada carpeta:
cd ms-gestionpacientes
mvnw clean install

cd ..\ms-notificaciones
mvnw clean install

cd ..\ms-optimizacion
mvnw clean install

cd ..\eureka-server
mvnw clean install

cd ..\api-gateway
mvnw clean install
```

**Checklist:**
- [ ] Todo compila sin errores

### PASO 4.2: Levantar servicios
```powershell
# Terminal 1 - Eureka
cd eureka-server
mvnw spring-boot:run

# Terminal 2 - API Gateway
cd api-gateway
mvnw spring-boot:run

# Terminal 3 - ms-gestionpacientes
cd ms-gestionpacientes
mvnw spring-boot:run

# Terminal 4 - ms-notificaciones
cd ms-notificaciones
mvnw spring-boot:run

# Terminal 5 - ms-optimizacion
cd ms-optimizacion
mvnw spring-boot:run
```

### PASO 4.3: Test manual
```powershell
# Registrar paciente
curl -X POST http://localhost:8080/pacientes \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Juan",
    "apellido": "Pérez",
    "dni": "12345678",
    "email": "juan@example.com"
  }'

# Verificar paciente registrado
curl http://localhost:8080/pacientes

# Verificar notificación creada
curl http://localhost:8080/api/notifications/pending
```

**Checklist:**
- [ ] Servicios levantados sin errores
- [ ] Curl registra paciente exitosamente
- [ ] Notificación aparece en ms-notificaciones
- [ ] Lista de espera actualizada

---

## 📋 FASE 5: DOCUMENTACIÓN (30 MINUTOS)

### PASO 5.1: Actualizar ARCHITECTURE.md

Agregar sección:
```markdown
## Flujo E2E Actual

1. POST /pacientes → ms-gestionpacientes
2. Automáticamente crea notificación
3. Datos guardados en Insforge
4. Consultar /api/notifications → ver notificaciones
5. Optimización consulta lista de espera
6. Asigna cita
7. Notifica cambio

## Integración con Insforge

- API Key: ${INSFORGE_API_KEY}
- Base URL: ${INSFORGE_API_URL}
- Modelos: Pacientes, Notificaciones, Citas
```

**Checklist:**
- [ ] ARCHITECTURE.md actualizado
- [ ] Diagrama de flujo agregado
- [ ] Credenciales documentadas (SIN valores reales)

---

## 🎯 RESUMEN DE CAMBIOS

| Fase | Tarea | Archivo | Tiempo |
|------|-------|---------|--------|
| 1.1 | Inyectar NotificationClient | PacienteService.java | 30 min |
| 1.2 | Crear NotificationClient | ms-optimizacion/ | 45 min |
| 1.3 | Verificar DTOs | .../dto/ | 15 min |
| 2.1 | Actualizar Spring Boot | ms-notificaciones/pom.xml | 1.5 h |
| 3.1 | Obtener Insforge | Insforge.io | 30 min |
| 3.2 | Crear entidades | Insforge dashboard | 45 min |
| 3.3 | Configurar Insforge | InsforgeService.java | 1 h |
| 3.4 | Variables de entorno | .env | 15 min |
| 4 | Testing | Curl + Verificación | 1 h |
| 5 | Documentación | ARCHITECTURE.md | 30 min |
| **TOTAL** | | | **6-7 h** |

---

## ✅ CHECKLIST FINAL

### ANTES DE COMENZAR
- [ ] Leíste este documento
- [ ] Tienes credenciales de Insforge
- [ ] Tienes acceso al IDE

### FASE 1 COMPLETA
- [ ] PacienteService.java modificado
- [ ] OptimizacionService.java modificado
- [ ] NotificationClient creado en ms-optimizacion
- [ ] NotificationRequestDTO creado en ambos servicios
- [ ] Todo compila

### FASE 2 COMPLETA
- [ ] pom.xml de ms-notificaciones actualizado
- [ ] Spring Boot 4.0.4
- [ ] Java 17
- [ ] spring-cloud 2025.1.1
- [ ] Compilación exitosa

### FASE 3 COMPLETA
- [ ] Cuenta en Insforge creada
- [ ] Proyecto en Insforge creado
- [ ] Credenciales obtenidas
- [ ] InsforgeService creado en 3 servicios
- [ ] application.yml actualizado
- [ ] .env creado con credenciales
- [ ] Modelos creados en Insforge

### FASE 4 COMPLETA
- [ ] Todo compila
- [ ] Servicios levantados
- [ ] Curl registra paciente
- [ ] Notificación creada
- [ ] Datos en Insforge

### FASE 5 COMPLETA
- [ ] ARCHITECTURE.md actualizado
- [ ] Documentación lista

### LISTO PARA PRESENTAR
- [ ] Sistema funcional
- [ ] Documentado
- [ ] Explicable a profesor
- [ ] Sin complejidad innecesaria

---

## 🤔 SI ALGO FALLA

| Problema | Solución |
|----------|----------|
| No compila | Verificar imports y nombres de clases |
| Notificación no se crea | Verificar Feign está habilitado y Eureka funciona |
| Insforge no responde | Verificar API key y URL |
| Datos no persisten | Verificar InsforgeService está siendo llamado |

---

## 📞 PREGUNTAS MÁS COMUNES

**P: ¿Debo hacer todo en un día?**  
R: No. Puedes hacer Fase 1 hoy, Fase 2 mañana, Fase 3 al día siguiente.

**P: ¿Puedo saltarme Insforge?**  
R: No, tu profesor lo pidió. Pero es factible, es solo una API REST.

**P: ¿Es muy complejo?**  
R: No, especialmente Fases 1 y 2. Fase 3 es media porque Insforge es nuevo, pero tienes IA para ayudarte.

**P: ¿Qué pasa si no puedo completar Insforge?**  
R: Las fases 1 y 2 son suficientes para mostrar integración. Fase 3 es extra.

---

**¿Listo para comenzar FASE 1?** 🚀

La próxima vez que me hables, simplemente dime:
- "FASE 1 completada" o "tengo errores en FASE 1"
- "FASE 2 completada" o "tengo errores en FASE 2"
- etc.

Y yo te ayudaré a resolver cualquier problema. ✨

