# 📋 Análisis Detallado de Arquitectura de Microservicios - RedNorte

**Fecha:** 28 de Abril, 2026  
**Evaluación:** Fullstack III - Modernización de Sistema de Salud  
**Estado:** ⚠️ CONEXIONES INCOMPLETAS - Requiere Integración

---

## 1️⃣ RESUMEN EJECUTIVO

Tu proyecto tiene **la estructura base correcta pero NO está completamente integrado**. Los tres microservicios existen de forma aislada con algunas conexiones a medias.

### 🎯 Situación Actual:
- ✅ Eureka Server funcionando para service discovery
- ✅ API Gateway enrutando correctamente
- ✅ Clientes Feign creados pero **NO UTILIZADOS**
- ❌ **ms-gestionpacientes NO notifica al registrar pacientes**
- ❌ **ms-optimizacion puede consultar lista de espera pero no notifica cambios**
- ❌ **ms-notificaciones NO tiene cliente hacia otros servicios**
- ❌ Versiones de Spring Boot inconsistentes (4.0.4 vs 2.7.12)
- ❌ Todos usan H2 en memoria (no son persistentes)

---

## 2️⃣ ANÁLISIS POR MICROSERVICIO

### 📱 MS-GESTIONPACIENTES (Puerto 8083)
**Rol:** Principal - Gestión de Pacientes y Lista de Espera

#### Qué hace:
- Registra pacientes (`POST /pacientes`)
- Gestiona lista de espera
- CRUD completo de pacientes
- Swagger eliminado del proyecto

#### Dependencias Importantes:
- Spring Boot 4.0.4 (Java 17)
- OpenFeign + Eureka Client ✅
  - Spring Data JPA + PostgreSQL + H2

#### Integraciones:
```java
// Existe NotificationClient pero NO se usa
@FeignClient(name = "ms-notificaciones")
interface NotificationClient {
    @PostMapping("/api/notifications")
    ResponseEntity<Void> createNotification(NotificationRequestDTO request);
}
```

**🔴 PROBLEMA:** En `PacienteService.registrarPaciente()` (líneas 19-23), solo guarda el paciente pero **NO crea una notificación**.

---

### 🔔 MS-NOTIFICACIONES (Puerto 8085)
**Rol:** Servicio de Notificaciones

#### Qué hace:
- Crea notificaciones (`POST /api/notifications`)
- Gestiona estados: PENDIENTE, ENVIADA
- Soporta 3 canales: EMAIL, SMS, PUSH
- Scheduler automático para envío
- Búsqueda por paciente y estado

#### Dependencias Importantes:
- Spring Boot **2.7.12** (Java 11) ⚠️ **DESINCRONIZADO**
- OpenFeign + Eureka Client ✅
- Spring Data JPA + PostgreSQL + H2
- Scheduling habilitado ✅

#### Integraciones:
```java
@EnableFeignClients  // Disponible pero...
// ❌ NO TIENE NINGUN CLIENTE FEIGN DEFINIDO
```

**🔴 PROBLEMA:** No puede comunicarse con otros servicios. No sabe si un paciente existe, si una cita fue asignada, etc.

---

### ⚡ MS-OPTIMIZACION (Puerto 8084)
**Rol:** Optimización de Citas y Recursos

#### Qué hace:
- Gestiona citas médicas
- Asigna horarios a médicos
- Implementa 3 estrategias: FIFO, POR_GRAVEDAD, OPTIMIZADA
- Cancela y reasigna citas

#### Dependencias Importantes:
- Spring Boot 4.0.4 (Java 17)
- OpenFeign + Eureka Client ✅
- Circuit Breaker (Resilience4j) ✅
- Spring Data JPA + PostgreSQL + H2

#### Integraciones:
```java
@FeignClient(name = "ms-listas-espera")
@CircuitBreaker(name = "listaEsperaService")
interface ListaEsperaClient {
    @GetMapping("/lista-espera")
    List<ListaEsperaDTO> getListaEspera();
}
```

✅ **Bien implementado:** Usa CircuitBreaker para resiliencia.

**🔴 PROBLEMA:** No notifica a nadie cuando reasigna citas. `OptimizacionService.procesarCancelacion()` solo actualiza BD.

---

## 3️⃣ DIAGRAMA DE FLUJO ACTUAL vs ESPERADO

### ❌ FLUJO ACTUAL (Desconectado)
```
[Paciente registra]
        ↓
[ms-gestionpacientes]
        ↓
[Base de datos local]
        ✋ FIN - No pasa nada más
```

### ✅ FLUJO ESPERADO (Conectado)
```
[POST /pacientes - Nuevo Paciente]
        ↓
[ms-gestionpacientes registra]
        ↓
[→ POST /api/notifications] Crear notificación "PACIENTE_REGISTRADO"
        ↓
[ms-notificaciones procesa]
        ↓
[→ GET ms-optimizacion/lista-espera] Consultar disponibilidad
        ↓
[ms-optimizacion asigna cita según estrategia]
        ↓
[→ POST /api/notifications] Crear notificación "CITA_ASIGNADA"
        ↓
[ms-notificaciones envía EMAIL/SMS/PUSH]
        ↓
[✅ PACIENTE NOTIFICADO CON CITA]
```

---

## 4️⃣ PROBLEMAS TÉCNICOS IDENTIFICADOS

### 🔴 Crítico: Clientes Feign No Utilizados
| Servicio | Cliente | Ubicación | Usado | Impacto |
|----------|---------|-----------|-------|---------|
| ms-gestionpacientes | `NotificationClient` | `client/` | ❌ NO | Pacientes sin notificaciones |
| ms-optimizacion | `ListaEsperaClient` | `client/` | ✅ SÍ | Consulta funciona |
| ms-notificaciones | - | - | - | No puede iniciar comunicación |

### ⚠️ Inconsistencias de Versionado
```
Spring Boot:
  - gestionpacientes: 4.0.4 (Java 17)
  - optimizacion:    4.0.4 (Java 17)
  - notificaciones:  2.7.12 (Java 11) ← DESINCRONIZADO

Cloud:
  - gestionpacientes: 2025.1.1
  - optimizacion:    2025.1.1
  - notificaciones:  2021.0.8 ← DESINCRONIZADO

Swagger: eliminado del proyecto
```

### 🗄️ Base de Datos: H2 (En Memoria, No Persistente)
Todos los servicios usan H2 por defecto:
```yaml
datasource:
  url: jdbc:h2:mem:testdb
```

**Problema:** Todos los datos se pierden al reiniciar. No es adecuado para producción.

---

## 5️⃣ REQUISITOS PARA MEJORAR

### 📊 Insforge - Base de Datos Recomendada
**Necesito confirmación tuya:**
1. ¿Es Insforge compatible con PostgreSQL driver estándar?
2. ¿O necesita un driver específico?
3. ¿Tienes credenciales/URL de conexión de Insforge?

**Asumiendo compatibilidad PostgreSQL:**
```yaml
# Configuración para Insforge
spring:
  datasource:
    url: jdbc:postgresql://insforge-host:5432/saludrednorte
    driverClassName: org.postgresql.Driver
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  jpa:
    database-platform: org.hibernate.dialect.PostgreSQL10Dialect
```

---

## 6️⃣ RECOMENDACIONES DE CAMBIOS

### 🎯 Prioridad 1: Completar Integraciones (INMEDIATO)

#### 1.1 ms-gestionpacientes → Notificar al registrar paciente
**Archivo:** `ms-gestionpacientes/src/main/java/.../service/PacienteService.java`

```java
@Service
public class PacienteService {
    @Autowired
    private PacienteRepository pacienteRepository;
    
    @Autowired
    private NotificationClient notificationClient;  // ← INYECTAR CLIENTE
    
    public Paciente registrarPaciente(Paciente paciente) {
        if (paciente.getDni() != null && pacienteRepository.existsByDniIgnoreCase(paciente.getDni())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un paciente con el DNI indicado");
        }
        
        Paciente savedPaciente = pacienteRepository.save(paciente);
        
        // ← CREAR NOTIFICACIÓN
        try {
            NotificationRequestDTO notif = new NotificationRequestDTO();
            notif.setPacienteId(savedPaciente.getId());
            notif.setTipo("PACIENTE_REGISTRADO");
            notif.setMensaje("Paciente " + savedPaciente.getNombre() + " registrado exitosamente");
            notif.setCanal("EMAIL");
            notificationClient.createNotification(notif);
        } catch (Exception e) {
            // Log pero no fallar el registro
            logger.warn("No se pudo crear notificación para paciente {}", savedPaciente.getId(), e);
        }
        
        return savedPaciente;
    }
}
```

#### 1.2 ms-optimizacion → Notificar al asignar cita
**Archivo:** `ms-optimizacion/src/main/java/.../service/OptimizacionService.java`

```java
// Crear cliente Feign hacia notificaciones
@FeignClient(name = "ms-notificaciones")
interface NotificationClient {
    @PostMapping("/api/notifications")
    ResponseEntity<Void> createNotification(@RequestBody NotificationRequestDTO request);
}

// En OptimizacionService:
@Autowired
private NotificationClient notificationClient;

public void procesarCancelacion(Long citaId, String estrategiaTipo) {
    citaService.cancelarCita(citaId);
    Cita citaCancelada = citaService.obtenerCitaPorId(citaId).orElse(null);
    if (citaCancelada != null) {
        EstrategiaOptimizacion estrategia = factory.getEstrategia(estrategiaTipo);
        Cita citaReasignada = estrategia.reasignarCita(citaCancelada);  // ← Retorna cita
        
        // ← NOTIFICAR NUEVA ASIGNACIÓN
        try {
            NotificationRequestDTO notif = new NotificationRequestDTO();
            notif.setPacienteId(citaReasignada.getPacienteId());
            notif.setTipo("CITA_REASIGNADA");
            notif.setMensaje("Cita reasignada para " + citaReasignada.getFecha());
            notif.setCanal("SMS");
            notificationClient.createNotification(notif);
        } catch (Exception e) {
            logger.warn("No se pudo notificar reasignación", e);
        }
    }
}
```

#### 1.3 ms-notificaciones → Integrar con otros servicios (Opcional pero recomendado)
Crear clientes para:
- Verificar existencia de paciente en ms-gestionpacientes
- Consultar detalles de cita en ms-optimizacion

### 🎯 Prioridad 2: Sincronizar Versiones

#### 2.1 Actualizar ms-notificaciones a Spring Boot 4.0.4
- Cambiar pom.xml parent de 2.7.12 → 4.0.4
- Java 11 → 17
- spring-cloud de 2021.0.8 → 2025.1.1

### 🎯 Prioridad 3: Implementar Insforge
Una vez que tengas credenciales:
- Crear `application-postgres.yml` en cada servicio
- Configurar variables de entorno
- Actualizar docker-compose.yml
- Migrar datos

### 🎯 Prioridad 4: Mejorar Resiliencia
- Agregar CircuitBreaker a ms-gestionpacientes cuando llama a notificaciones
- Implementar retry policy
- Agregar logging detallado

---

## 7️⃣ CHECKLIST DE IMPLEMENTACIÓN

- [ ] Inyectar `NotificationClient` en `PacienteService`
- [ ] Implementar notificación en `registrarPaciente()`
- [ ] Crear `NotificationClient` en ms-optimizacion
- [ ] Implementar notificación en reasignación de citas
- [ ] Sincronizar Spring Boot 4.0.4 en todos los servicios
- [ ] Sincronizar spring-cloud en todos los servicios
- [ ] Configurar Insforge con credenciales reales
- [ ] Migrar BD de H2 a Insforge en todos los servicios
- [ ] Ejecutar smoke tests
- [ ] Actualizar ARCHITECTURE.md con nuevo flujo

---

## 8️⃣ IMPACTO ESTIMADO

| Cambio | Tiempo | Complejidad | Riesgo |
|--------|--------|-------------|--------|
| Integración PacienteService | 30 min | Baja | Mínimo |
| Integración OptimizacionService | 45 min | Media | Bajo |
| Sincronizar Spring Boot | 1.5 h | Baja | Bajo |
| Implementar Insforge | 2-3 h | Alta | Medio |
| Tests E2E | 1 h | Media | Bajo |
| **TOTAL ESTIMADO** | **6-7 h** | **Media** | **Bajo** |

---

## 9️⃣ PRÓXIMOS PASOS SUGERIDOS

1. **Confirma detalles sobre Insforge:**
   - Tipo de BD (PostgreSQL, MySQL, etc.)
   - Credenciales de conexión
   - Requirements de setup

2. **Responde preguntas:**
   - ¿Necesitas manejo de transacciones distribuidas (Saga pattern)?
   - ¿Hay tests existentes que dependan de H2?
   - ¿Hay deadline específico?

3. **Ejecuta cambios en orden:**
   - Prioridad 1 (Integraciones)
   - Prioridad 2 (Versionado)
   - Prioridad 3 (Insforge)
   - Prioridad 4 (Resiliencia)

---

**Conclusión:** Tu arquitectura está **80% lista**. Solo necesita conectar las piezas que ya existen, sincronizar versiones e implementar la BD persistente.

