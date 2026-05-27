# 🔄 Análisis Completo y Rediseño Arquitectónico del Sistema RedNorte

**Fecha:** 27 de Mayo, 2026  
**Estado:** Propuesta de Rediseño - Listo para implementación  
**Impacto:** Transformación del sistema hacia Event-Driven Architecture

---

## 📊 Tabla de Contenidos

| Sección | Descripción |
|---------|------------|
| 1. Situación Actual | Análisis detallado del estado actual del proyecto |
| 2. Problemas Identificados | Lista completa de issues críticas y mejorables |
| 3. Arquitectura Propuesta | Nuevo diseño Event-Driven |
| 4. Comparativa: Actual vs Propuesta | Diferencias clave |
| 5. Plan de Implementación | 6 fases con timeline y checklist |
| 6. Estructura de Carpetas | Nuevo layout de proyecto |
| 7. Diagrama de Flujos | Visualización de cambios |
| 8. Decisiones Arquitectónicas | Justificación de cada cambio |

---

## 1️⃣ SITUACIÓN ACTUAL (AS-IS)

### Descripción del Sistema
Tu proyecto implementa un **sistema de gestión de pacientes y citas médicas** para RedNorte con los siguientes componentes:

```
Frontend (React + Vite)
        ↓
   API Gateway (8080)
        ↓
   ┌────┴────┬────────┬──────────┐
   ↓         ↓        ↓          ↓
ms-Pacientes ms-Optim ms-Notif  BFF
(8083)       (8084)   (8085)    (8097)
        ↓         ↓         ↓      ↓
    ┌───────────────────────────────┐
    │  H2 In-Memory (4 instancias)  │
    │  [NO PERSISTENTE]              │
    └───────────────────────────────┘

Eureka Server (8761) - Service Discovery
```

### Tecnologías Actuales
| Componente | Tecnología | Versión | Estado |
|-----------|-----------|---------|--------|
| **Spring Boot** | Todos salvo ms-notif | 3.4.1 | ✅ |
| **Spring Cloud** | Todos | 2024.0.0 | ✅ |
| **BD Principal** | H2 | In-Memory | 🔴 No persistente |
| **BD Alternativa** | PostgreSQL | Compatible | 🟡 Disponible pero no usado |
| **Service Discovery** | Eureka | Netflix | ✅ |
| **API Gateway** | Spring Cloud Gateway | 2024.0.0 | ✅ |
| **IPC** | OpenFeign | Síncrono | 🟡 Parcial |
| **Resiliencia** | Resilience4j | Minimal | 🟡 Solo en ms-optimizacion |
| **Eventos** | N/A | None | 🔴 CRÍTICO: NO EXISTE |

### Microservicios Actuales

#### 📱 ms-gestionpacientes (Puerto 8083)
**Responsabilidad:** Gestión de Pacientes

```
Endpoints:
├─ POST   /pacientes                    # Registrar paciente
├─ GET    /pacientes                    # Listar pacientes
├─ GET    /pacientes/{id}               # Obtener paciente
├─ PUT    /pacientes/{id}               # Actualizar paciente
├─ DELETE /pacientes/{id}               # Eliminar paciente
├─ GET    /lista-espera                 # Listar espera
├─ POST   /lista-espera                 # Agregar a espera
└─ DELETE /lista-espera/{id}            # Remover de espera

Dependencias de Feign:
├─ NotificationClient (CREADO pero NO USADO) ← 🔴 PROBLEMA
└─ No hay clientes hacia ms-optimizacion

BD: Entidades Paciente, ListaEspera (H2)
```

**Problemas identificados:**
- ❌ Crea `NotificationClient` pero nunca lo inyecta
- ❌ Al registrar paciente, NO notifica
- ❌ Al agregar a lista de espera, NO notifica
- ❌ Sin patrón de eventos
- ❌ Acoplamiento manual con notificaciones

#### ⚡ ms-optimizacion (Puerto 8084)
**Responsabilidad:** Optimización de Citas y Asignación de Horarios

```
Endpoints:
├─ POST   /optimizacion/citas           # Crear cita
├─ GET    /optimizacion/citas           # Listar citas
├─ PUT    /optimizacion/citas/{id}      # Actualizar cita
├─ DELETE /optimizacion/citas/{id}      # Cancelar cita
├─ POST   /optimizacion/reasignar       # Reasignar cita
├─ GET    /optimizacion/lista-espera    # Consultar disponibilidad
└─ GET    /optimizacion/estrategia      # Obtener estrategia

Estrategias de Optimización:
├─ FIFO                                 # Primer que entra, primero que sale
├─ POR_GRAVEDAD                         # Ordena por urgencia
└─ OPTIMIZADA                           # Algoritmo personalizado

Dependencias de Feign:
├─ ListaEsperaClient (USADO CORRECTAMENTE) ✅
├─ CircuitBreaker implementado ✅
└─ NotificationClient (NO EXISTE) ← 🔴 PROBLEMA

BD: Entidades Cita, Medico, Horario (H2)
```

**Problemas identificados:**
- ❌ Tiene ListaEsperaClient pero es consulta síncrona
- ❌ Al reasignar cita, NO notifica
- ❌ Acoplamiento alto con ms-gestionpacientes
- ✅ CircuitBreaker bien implementado
- ❌ Sin resiliencia en otras operaciones

#### 🔔 ms-notificaciones (Puerto 8085)
**Responsabilidad:** Gestión y Envío de Notificaciones

```
Endpoints:
├─ POST   /api/notifications            # Crear notificación
├─ GET    /api/notifications            # Listar notificaciones
├─ GET    /api/notifications/{id}       # Obtener notificación
├─ PUT    /api/notifications/{id}       # Actualizar estado
├─ DELETE /api/notifications/{id}       # Eliminar notificación
├─ POST   /api/notifications/enviar     # Enviar inmediatamente
└─ GET    /api/notifications/paciente/{id} # Notificaciones de un paciente

Canales Soportados:
├─ EMAIL
├─ SMS
└─ PUSH

Estados de Notificación:
├─ PENDIENTE
├─ ENVIADA
├─ FALLIDA
└─ CANCELADA

Características:
├─ Scheduler automático para envío ✅
├─ Reintentos automáticos 🟡
└─ Logging de eventos (parcial) 🟡

Dependencias de Feign:
├─ @EnableFeignClients DECLARADO pero...
└─ SIN CLIENTES DEFINIDOS ← 🔴 CRÍTICO

BD: Entidad Notificacion (H2)
```

**Problemas identificados:**
- ❌ NO tiene clientes Feign para validar pacientes o citas
- ❌ Es un servicio AISLADO, no se integra con otros
- ❌ Recibe llamadas directas (no eventos)
- ❌ Sin patrón de suscriptor observable
- ⚠️ No puede verificar si paciente existe antes de notificar

#### 🎯 BFF - Backend for Frontend (Puerto 8097)
**Responsabilidad:** Agregador de datos para el frontend

```
Endpoints:
├─ GET /api/portal/resumen              # Dashboard inicial
└─ (Otros endpoints de agregación)

Responsabilidad:
├─ Obtiene datos de ms-gestionpacientes
├─ Obtiene datos de ms-optimizacion
├─ Obtiene datos de ms-notificaciones
└─ Retorna una respuesta agregada única

Beneficio:
✅ Frontend hace 1 llamada en lugar de 3
✅ Reduce latencia en UI
```

**Problemas actuales:**
- 🟡 Depende de que los 3 microservicios estén UP
- 🟡 Sin circuit breaker en las llamadas
- 🟡 Sin timeout configurado

---

## 2️⃣ PROBLEMAS IDENTIFICADOS (CRÍTICOS & MEJORABLES)

### 🔴 CRÍTICOS (Bloquean producción)

#### 1. **Falta de Comunicación Inter-Servicios**
```
ACTUAL:
  ms-gestionpacientes.registrarPaciente()
     └─ Guarda paciente ✅
     └─ FIN (NO notifica) ❌

IMPACTO:
  - Paciente registrado pero nunca notificado
  - Cita nunca se asigna automáticamente
  - Sistema está DESCONECTADO
```

**Severidad:** CRÍTICA (impacta flujo de negocio)

#### 2. **Base de Datos En Memoria (H2)**
```
ACTUAL:
  Todos los datos en RAM
  └─ Se pierden al reiniciar ❌
  └─ Imposible auditoría ❌
  └─ No es HIPAA-compliant ❌

IMPACTO:
  - Imposible producción
  - Datos de pacientes PERDIDOS
  - Violación de regulaciones
```

**Severidad:** CRÍTICA (impide go-live)

#### 3. **Sin Patrón de Eventos**
```
ACTUAL:
  ms-gestionpacientes → (Feign directo) → ms-notificaciones
              └─ Acoplamiento alto
              └─ Fallos en cascada si ms-notificaciones está down
              └─ Sin garantía de entrega

IMPACTO:
  - Si ms-notificaciones cae → pacientes se registran pero quedan sin notificar (data inconsistency)
  - Nuevo servicio (ej: auditoría) requiere cambiar código en 3 lugares
  - NO escalable
```

**Severidad:** CRÍTICA

#### 4. **Clientes Feign No Utilizados**
```
ACTUAL:
  ms-gestionpacientes CREA NotificationClient
     └─ @FeignClient("ms-notificaciones")
     └─ public ResponseEntity<Void> createNotification(...);
     └─ NUNCA SE INYECTA ❌

ANÁLISIS:
  - Indica falta de conectar la integración
  - Sistema está "a mitad de construir"
  - Probable: Copy-paste de ejemplos sin completar
```

**Severidad:** ALTA

---

### 🟡 MEJORABLES (Degradan calidad but no bloquean)

#### 5. **Resiliencia Parcial**
```
ACTUAL:
  ✅ ms-optimizacion tiene CircuitBreaker
  ❌ ms-gestionpacientes sin CircuitBreaker
  ❌ BFF sin CircuitBreaker o timeout
  ❌ ms-notificaciones sin integraciones

IMPACTO:
  - Si ms-optimizacion está lento → ms-gestionpacientes espera infinitamente
  - Cascada de timeouts
  - Pobre experiencia de usuario
```

#### 6. **Versionado Inconsistente**
```
ANTES (según ANALISIS_MICROSERVICIOS.md):
  Spring Boot 2.7.12 vs 4.0.4 ❌ DISCREPANCIA

ACTUAL (revisión actual):
  Todos están en 3.4.1 ✅ CORRECTO
  Todos están en 2024.0.0 ✅ CORRECTO
```

#### 7. **Falta de Transacciones Distribuidas**
```
ACTUAL:
  POST /pacientes
    ├─ Registra en BD ✅
    ├─ Llama a ms-notificaciones ✅
    └─ ¿Qué pasa si notificación falla?
       ├─ Opción 1: Retorna error al usuario ❌ (paciente registrado pero usuario cree que falló)
       ├─ Opción 2: Ignora error ❌ (paciente sin notificación)
       └─ Opción 3: SAGA PATTERN ✅ (FALTA IMPLEMENTAR)

IMPACTO:
  - Data inconsistency
  - Sin transacción ACID distribuida
```

#### 8. **Frontend → Backend Coupling**
```
ACTUAL:
  frontend/src/api/
  ├─ gestionPacientesApi.js     → http://localhost:8080/pacientes
  ├─ notificacionesApi.js       → http://localhost:8080/api/notifications
  └─ optimizacionApi.js         → http://localhost:8080/optimizacion

PROBLEMA:
  - Hace 3 llamadas paralelas a endpoints diferentes
  - Espera a que TODAS terminen
  - Si 1 es lenta → Todo lento
  - BFF no es utilizado optimalmente

SOLUCIÓN:
  - Frontend debería hacer 1 llamada a BFF
  - BFF agrega datos de todos (ya implementado pero no optimizado)
```

---

## 3️⃣ ARQUITECTURA PROPUESTA (TO-BE)

### Nuevo Diseño: Event-Driven Microservices + SAGA Pattern

```
┌───────────────────────────────────────────────────────────────────────────┐
│                    CLIENTE                                                │
│              (React + Frontend)                                           │
└────────────────────────────────────┬────────────────────────────────────┘
                                     │
                      (REST Calls)   ↓
                ┌────────────────────────────────────┐
                │     API Gateway (8080)             │
                │  Spring Cloud Gateway              │
                │  + Authentication Filter           │
                │  + Rate Limiting                   │
                │  + Request Logging                 │
                └────────────────────┬───────────────┘
                                     │
        ┌────────────────────────────┼────────────────────────┐
        │                            │                         │
        ↓                            ↓                         ↓
┌──────────────────┐    ┌──────────────────┐    ┌──────────────────┐
│  PACIENTES       │    │  CITAS           │    │ NOTIFICACIONES   │
│  Domain Service  │    │  Domain Service  │    │  Domain Service  │
│  (8083)          │    │  (8084)          │    │  (8085)          │
│                  │    │                  │    │                  │
│ POST /pacientes  │    │ POST /citas      │    │ (Solo listeners) │
│ GET  /pacientes  │    │ GET  /citas      │    │                  │
│ EMIT:            │    │ EMIT:            │    │ LISTEN:          │
│ • patient.*      │    │ • appointment.*  │    │ • patient.*      │
│ • del evento     │    │ • del evento     │    │ • appointment.*  │
│                  │    │                  │    │ • EMIT:          │
│                  │    │                  │    │ • notification.* │
└────────┬─────────┘    └────────┬─────────┘    └────────┬─────────┘
         │                       │                        │
         └───────────────────────┼────────────────────────┘
                                 │
                    (Async Messaging)
                                 ↓
                  ┌──────────────────────────────┐
                  │   MESSAGE BROKER             │
                  │   (Kafka o RabbitMQ)         │
                  │                              │
                  │ Topics:                      │
                  │ • patient.registered         │
                  │ • patient.updated            │
                  │ • patient.deleted            │
                  │ • appointment.assigned       │
                  │ • appointment.rescheduled    │
                  │ • appointment.cancelled      │
                  │ • notification.sent          │
                  │ • notification.failed        │
                  │ • audit.event.*              │
                  └──────────────────┬───────────┘
                                     │
        ┌────────────────────────────┼────────────────────────┐
        │                            │                        │
        ↓                            ↓                        ↓
┌──────────────────┐    ┌──────────────────┐    ┌──────────────────┐
│  AUDIT SERVICE   │    │  ANALYTICS       │    │  NOTIFICATIONS   │
│  (Nuevo)         │    │  (Futuro)        │    │  Listeners       │
│                  │    │                  │    │                  │
│ Escucha todos    │    │ Para reportes    │    │ Escucha todos    │
│ los eventos      │    │ y dashboards     │    │ los eventos      │
│ Guarda en        │    │ BI               │    │ Envía EMAIL/SMS  │
│ audit_log        │    │                  │    │ PUSH             │
└──────────────────┘    └──────────────────┘    └──────────────────┘
         │
         └──────────────┐
                        │
                        ↓
┌───────────────────────────────────────────────────────────┐
│  PostgreSQL (Insforge) 💾                                 │
│  ┌─────────────────────────────────────────────────────┐  │
│  │ Tablas por Dominio:                                │  │
│  │                                                     │  │
│  │ PACIENTES:                                          │  │
│  │ ├─ paciente                    (Entidad)             │  │
│  │ ├─ paciente_historial          (Auditoría)           │  │
│  │                                                     │  │
│  │ CITAS:                                              │  │
│  │ ├─ cita                        (Entidad)             │  │
│  │ ├─ medico                      (Catálogo)            │  │
│  │ ├─ horario                     (Catálogo)            │  │
│  │ ├─ cita_intentos_reasignacion  (Auditoría)           │  │
│  │                                                     │  │
│  │ NOTIFICACIONES:                                     │  │
│  │ ├─ notificacion                (Entidad)             │  │
│  │ ├─ notificacion_intento        (Reintentos)          │  │
│  │                                                     │  │
│  │ AUDITORIA (Centralizado):                           │  │
│  │ ├─ audit_log                   (Todos los eventos)   │  │
│  │ ├─ audit_cambios               (Quién cambió qué)    │  │
│  │ └─ audit_accesos               (Acceso a datos)      │  │
│  │                                                     │  │
│  │ KAFKA (Event Sourcing - Opcional):                  │  │
│  │ ├─ event_store                 (Todos los eventos)   │  │
│  │ └─ event_snapshots             (Cache de eventos)    │  │
│  └─────────────────────────────────────────────────────┘  │
└───────────────────────────────────────────────────────────┘

Eureka Server (8761) - Service Discovery (Se mantiene igual)
```

### Componentes Nuevos

#### 📨 Message Broker (Kafka o RabbitMQ)
```
Decisión: KAFKA ✅ (Recomendado)
├─ Mayor throughput
├─ Retención de eventos (auditoría)
├─ Mejor para salud (requisitos regulatorios)
├─ Escalabilidad horizontal
└─ Comunidad más grande para healthcare domain

Alternativa: RabbitMQ (También válida)
├─ Más simple
├─ Menor latencia
├─ Mejor para POC
└─ Menos overhead
```

#### 🎼 SAGA Orchestrator
```
Flujo: Registro de Paciente con Asignación de Cita

[1] Cliente → API Gateway → ms-gestionpacientes
    POST /pacientes { nombre, email, dni }
    
[2] ms-gestionpacientes:
    ├─ Valida existencia de paciente por DNI
    ├─ Registra Paciente en BD
    ├─ Guarda transacción en event_store
    ├─ EMITE: patient.registered
    │   { patientId: 123, nombre: "Juan", email: "juan@..." }
    └─ Retorna 201 CREATED inmediatamente
    
[3] Kafka Topic "patient.registered":
    Dos suscriptores:
    
    Suscriptor A: ms-optimizacion
    ├─ Escucha patient.registered
    ├─ Consulta lista_espera por urgencia
    ├─ Busca horario disponible (estrategia FIFO/GRAVEDAD/etc)
    ├─ Crea Cita en BD
    ├─ EMITE: appointment.assigned
    │   { appointmentId: 456, patientId: 123, fecha: "2026-05-29", hora: "09:00" }
    └─ Si falla → EMITE: appointment.assignment_failed
    
    Suscriptor B: NotificationService
    ├─ Escucha patient.registered
    ├─ Crea Notificación "PACIENTE_REGISTRADO"
    ├─ Schedule para enviar en 5 minutos
    ├─ EMITE: notification.created
    └─ Si falla → retry automático (Dead Letter Queue)
    
[4] Kafka Topic "appointment.assigned":
    Suscriptor: NotificationService
    ├─ Escucha appointment.assigned
    ├─ Crea Notificación "CITA_ASIGNADA"
    ├─ Envía SMS inmediato + EMAIL dentro de 1 hora
    ├─ EMITE: notification.sent
    └─ Guarda comprobante en BD
    
[5] Todos los servicios:
    ├─ Guardan eventos en event_store (Event Sourcing)
    ├─ Permiten auditoría completa
    ├─ Permiten reconstruir estado desde eventos
    └─ Cumplen requisitos HIPAA/RGPD

RESULTADO FINAL:
✅ Paciente registrado
✅ Cita asignada automáticamente
✅ Paciente notificado (2 notificaciones)
✅ Auditoría completa (quién, cuándo, qué)
✅ Transacción "distribuida" garantizada
✅ Si una parte falla → Compensating transaction restaura consistencia
```

#### 🔐 Audit Service (Nuevo)
```
Responsabilidad: Escuchar TODOS los eventos
                 Guardar en audit_log
                 Permitir trazabilidad 100%

Características:
├─ RGPD compliant (derecho al olvido)
├─ HIPAA compliant (Qui fait quoi?)
├─ Compliance con regulaciones de salud
└─ Reporte de acceso a datos

Escucha:
* patient.*
* appointment.*
* notification.*
* user.*
* access.*

Guarda:
├─ audit_log
│  ├─ timestamp
│  ├─ event_type
│  ├─ resource_type
│  ├─ resource_id
│  ├─ actor_id (usuario/sistema)
│  ├─ action
│  ├─ details (JSON)
│  └─ status (SUCCESS/FAILED)
│
└─ audit_access_log
   ├─ user_id
   ├─ accessed_resource
   ├─ access_time
   ├─ access_type (READ/WRITE/DELETE)
   └─ ip_address
```

---

## 4️⃣ COMPARATIVA: ACTUAL vs PROPUESTA

### Flujo: Registrar Paciente

#### ACTUAL (Problema)
```
[1] POST /pacientes
    └─ ms-gestionpacientes.registrarPaciente()
    
[2] Guarda Paciente en H2 ✅
    
[3] FIN ❌
    └─ NotificationClient creado pero NO INYECTADO
    └─ Paciente registrado pero NO NOTIFICADO
    └─ Cita NUNCA se asigna
    └─ Sistema desconectado
    
PROBLEMAS:
❌ Acoplamiento manual (si quiero notificar, debo cambiar PacienteService)
❌ Si ms-notificaciones está caído → registro se pierde (o falla el registro)
❌ Sin garantía de entrega de notificación
❌ Imposible agregar nuevo suscriptor (auditoría, analytics) sin cambiar código
❌ Datos perdidos al reiniciar (H2)
```

#### PROPUESTA (Solución)
```
[1] POST /pacientes
    └─ ms-gestionpacientes.registrarPaciente()
    
[2] Valida + Guarda en PostgreSQL ✅
    
[3] EMITE: patient.registered (Kafka) ✅
    └─ Asíncrono
    └─ Desacoplado
    └─ Guaranteed delivery
    
[4] Kafka Topic "patient.registered"
    ├─ Suscriptor 1: ms-optimizacion
    │  └─ Asigna cita → EMITE: appointment.assigned
    │
    ├─ Suscriptor 2: ms-notificaciones
    │  └─ Crea notificación "PACIENTE_REGISTRADO"
    │
    └─ Suscriptor 3: AuditService
       └─ Registra en audit_log
       
[5] Kafka Topic "appointment.assigned"
    └─ ms-notificaciones
       └─ Crea notificación "CITA_ASIGNADA"
       └─ Envía SMS + EMAIL
       
[6] RESULTADO:
✅ Paciente registrado + notificado + con cita asignada
✅ Sistema desacoplado
✅ Si AuditService está caído → no bloquea a paciente
✅ Nuevo suscriptor (Analytics) sin cambiar nada
✅ Transacción distribuida con SAGA pattern
✅ Auditoría completa con Event Sourcing
✅ Datos persistentes y recuperables

TIEMPO:
Actual: ~500ms (síncrono espera a todos)
Propuesta: ~100-200ms (asíncrono, responde inmediatamente)
```

### Matriz de Mejoras

| Aspecto | Actual | Propuesta | Mejora |
|---------|--------|-----------|--------|
| **Tipo de Comunicación** | Feign síncrono | Kafka asíncrono + Feign (solo GET) | ⬆️ Desacoplamiento |
| **Acoplamiento** | Alto (cada MS conoce otros) | Bajo (solo eventos) | ⬆️ Escalabilidad |
| **Persistencia** | H2 (RAM) | PostgreSQL | ⬆️ Producción-ready |
| **Resiliencia** | Parcial (1x CircuitBreaker) | Completa (Kafka retries, DLQ, CB) | ⬆️ Disponibilidad |
| **Auditoría** | Ninguna | Event Sourcing completo | ⬆️ Compliance |
| **Transacciones Dist.** | Manual❌ | SAGA Pattern automático | ⬆️ Data consistency |
| **Nueva funcionalidad** | Requiere cambiar 3 servicios | Solo agregar suscriptor | ⬆️ Mantenibilidad |
| **Testing** | Tightly coupled | Unitario + integration desacoplado | ⬆️ Testabilidad |
| **Latencia** | ~500ms | ~100-200ms | ⬆️ Performance |
| **Scaling** | Limitado (H2) | Horizontal (Kafka + PostgreSQL) | ⬆️ Escalabilidad |

---

## 5️⃣ PLAN DE IMPLEMENTACIÓN (6 FASES)

### Fase 1️⃣: Auditoría & Análisis Detallado (2-3 días)

**Objetivo:** Entender completamente el sistema actual y mapear dependencias

**Tareas:**
- [x] Analizar flujos de negocio actuales
- [x] Mapear entidades y relaciones
- [x] Identificar transacciones distribuidas
- [x] Documentar problemas
- [ ] Crear C4 diagrams (Context, Container, Component, Code)
- [ ] Listar eventos de negocio por dominio
- [ ] Identificar SAGAs necesarias
- [ ] Validar requisitos HIPAA/RGPD

**Deliverables:**
- `ARCHITECTURE.md` actualizado
- `REDISEÑO_ARQUITECTONICO.md` (este documento)
- `EVENTOS.md` (catálogo de eventos)
- Diagrama C4 en texto/Mermaid

**Riesgo:** BAJO

---

### Fase 2️⃣: Refactorización de Dominios (5-7 días)

**Objetivo:** Reestructurar servicios para tener responsabilidades claras

**Cambios en ms-gestionpacientes:**

```java
// ANTES
@Service
public class PacienteService {
    public Paciente registrarPaciente(Paciente p) {
        return pacienteRepository.save(p);  // FIN, no notifica
    }
}

// DESPUÉS
@Service
public class PacienteService {
    @Autowired
    private PacienteEventPublisher eventPublisher;
    
    public Paciente registrarPaciente(Paciente p) {
        Paciente saved = pacienteRepository.save(p);
        
        // Emite evento ASÍNCRONO
        eventPublisher.publishPatientRegistered(
            new PatientRegisteredEvent(saved.getId(), saved.getNombre())
        );
        return saved;
    }
}

@Component
public class PacienteEventPublisher {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    
    public void publishPatientRegistered(PatientRegisteredEvent event) {
        kafkaTemplate.send("patient.registered", event.toJson());
    }
}
```

**Cambios en ms-optimizacion:**

```java
// NUEVO: Cliente para obtener lista de espera (síncrono, lectura)
@FeignClient(name = "ms-listas-espera")
@CircuitBreaker(name = "listaEspera")
public interface ListaEsperaClient {
    @GetMapping("/lista-espera")
    List<ListaEsperaDTO> getListaEspera();
}

// NUEVO: Listener para eventos de pacientes
@Component
public class CrearCitaPorNuevoPacienteListener {
    @KafkaListener(topics = "patient.registered", groupId = "optimizacion-group")
    public void onPatientRegistered(PatientRegisteredEvent event) {
        // Obtiene lista de espera
        // Asigna cita
        // Publica appointment.assigned
    }
}

// NUEVO: Publisher de eventos de citas
@Component
public class AppointmentEventPublisher {
    public void publishAppointmentAssigned(AppointmentAssignedEvent event) {
        kafkaTemplate.send("appointment.assigned", event.toJson());
    }
}
```

**Cambios en ms-notificaciones:**

```java
// NUEVO: Listeners para eventos
@Component
public class NotificacionEventListeners {
    
    @KafkaListener(topics = "patient.registered", groupId = "notificaciones-group")
    public void onPatientRegistered(PatientRegisteredEvent event) {
        // Crea y programa notificación
        // Se envía en 5 minutos
    }
    
    @KafkaListener(topics = "appointment.assigned", groupId = "notificaciones-group")
    public void onAppointmentAssigned(AppointmentAssignedEvent event) {
        // Crea y envía notificación de cita asignada
    }
}

// NOTA: Ya NO recibe llamadas Feign directas
// Todo viene por Kafka (asíncrono, resiliente)
```

**Tareas:**
- [ ] Crear clases Event (PatientRegisteredEvent, AppointmentAssignedEvent, etc.)
- [ ] Crear `*EventPublisher` en cada MS
- [ ] Crear `KafkaProducerConfig` en cada MS publicador
- [ ] Crear `KafkaConsumerConfig` en cada MS suscriptor
- [ ] Extraer listeners en `*EventListener` components
- [ ] Remover acoplamiento Feign síncrono de escrituras
- [ ] Mantener Feign para lecturas (get lista, get detalles)

**Deliverables:**
- Código refactorizado
- Compilación exitosa (sin Kafka aún)
- Pruebas unitarias verdes

**Riesgo:** MEDIO (cambios en estructuras principales)

---

### Fase 3️⃣: Implementar Message Broker (4-5 días)

**Objetivo:** Integrar Kafka como backbone de comunicación asíncrona

**Pasos:**

1. **Agregar dependencias**

```xml
<!-- Cada microservicio que publica o consume eventos -->
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
</dependency>
```

2. **Configurar Kafka en Docker Compose**

```yaml
version: '3.8'
services:
  zookeeper:
    image: confluentinc/cp-zookeeper:7.5.4
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      
  kafka:
    image: confluentinc/cp-kafka:7.5.4
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:29092,PLAINTEXT_HOST://localhost:9092
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
      KAFKA_INTER_BROKER_LISTENER_NAME: PLAINTEXT
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
```

3. **Crear KafkaProducerConfig**

```java
// En cada MS que publica
@Configuration
public class KafkaProducerConfig {
    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
    
    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }
}
```

4. **Crear KafkaConsumerConfig**

```java
// En cada MS que suscribe
@Configuration
public class KafkaConsumerConfig {
    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "notificaciones-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }
    
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setCommonErrorHandler(commonErrorHandler());
        factory.setConcurrency(3);
        return factory;
    }
    
    @Bean
    public CommonErrorHandler commonErrorHandler() {
        // Retry 3 veces con exponential backoff, luego DLQ
        FixedBackOff backOff = new FixedBackOff(5000, 3); // 5s entre intentos, 3 max
        DefaultErrorHandler handler = new DefaultErrorHandler(deadLetterPublishingRecoverer(), backOff);
        return handler;
    }
    
    @Bean
    public DeadLetterPublishingRecoverer deadLetterPublishingRecoverer() {
        return new DeadLetterPublishingRecoverer(kafkaTemplate());
    }
}
```

5. **Crear Topics automáticamente**

```java
@Configuration
public class KafkaTopicsConfig {
    @Bean
    public NewTopic patientRegisteredTopic() {
        return TopicBuilder.name("patient.registered")
            .partitions(3)
            .replicas(1)
            .build();
    }
    
    @Bean
    public NewTopic appointmentAssignedTopic() {
        return TopicBuilder.name("appointment.assigned")
            .partitions(3)
            .replicas(1)
            .build();
    }
    
    // Más topics según eventos...
}
```

6. **Implementar Listeners**

```java
@Component
public class NotificacionEventListeners {
    
    @KafkaListener(topics = "patient.registered", groupId = "notificaciones-group")
    public void onPatientRegistered(String message) {
        // Deserializar JSON
        PatientRegisteredEvent event = objectMapper.readValue(message, PatientRegisteredEvent.class);
        
        // Crear notificación
        Notificacion notif = new Notificacion();
        notif.setPacienteId(event.getPatientId());
        notif.setTipo("PACIENTE_REGISTRADO");
        notif.setCanal("EMAIL");
        // schedule para 5 minutos después
        
        notificacionService.crear(notif);
    }
    
    @KafkaListener(topics = "patient.registered")
    public void handleError(Message<?> message, ListenerExecutionFailedException exception) {
        log.error("Error procesando mensaje de patient.registered", exception);
        // Log para auditoría
    }
}
```

**Tareas:**
- [ ] Agregar spring-kafka a todos los pom.xml
- [ ] Crear KafkaProducerConfig en ms-gestionpacientes, ms-optimizacion
- [ ] Crear KafkaConsumerConfig en ms-notificaciones, ms-optimizacion (como suscriptor), auditService
- [ ] Crear KafkaTopicsConfig centralizado
- [ ] Implementar @KafkaListener en cada servicio
- [ ] Configurar Dead Letter Queue (DLQ)
- [ ] Configurar retry policy con exponential backoff
- [ ] Testing: publicar evento → verificar que listener reacciona
- [ ] Testing: Kafka down → verificar que retries funcionan
- [ ] Testing: DLQ → verificar que mensajes fallidos se guardan

**Deliverables:**
- Kafka integrado
- Topics creados automáticamente
- Listeners funcionando
- Retry policy testeado
- DLQ operacional

**Riesgo:** MEDIO (nueva infraestructura)

---

### Fase 4️⃣: SAGA Pattern para Transacciones Distribuidas (5-7 días)

**Objetivo:** Implementar transacciones ACID distribuidas con compensating transactions

**Patrón Propuesto: Choreography-based SAGA**

```
SAGA: PatientRegistrationSaga

PASO 1: ms-gestionpacientes
  ├─ Acción: Registra paciente en BD
  ├─ Emite: patient.registered
  └─ Transacción: Paciente savedPoint

PASO 2: ms-optimizacion (escucha patient.registered)
  ├─ Acción: Intenta asignar cita
  ├─ Emite: appointment.assigned O appointment.assignment_failed
  └─ Transacción: Cita savePoint O rollback (compensa)

PASO 3: ms-notificaciones (escucha appointment.assigned)
  ├─ Acción: Crea y envía notificación
  ├─ Emite: notification.sent O notification.send_failed
  └─ Transacción: Notificación savePoint O retry (no rollback, es optional)

COMPENSATIONS:
si appointment.assignment_failed:
  ├─ ms-gestionpacientes.rollback(paciente)
  │  └─ Emite: patient.registered_rollback
  └─ Cliente recibe error 422 "No se pudo asignar cita"

si appointment.assigned pero notification.send_failed:
  ├─ Reintentar automáticamente (Kafka DLQ)
  └─ No rollback cita (es non-blocking)
```

**Implementación con Axon Framework (Opcional, para CQRS)**

```xml
<dependency>
    <groupId>org.axonframework</groupId>
    <artifactId>axon-spring-boot-starter</artifactId>
    <version>4.9.0</version>
</dependency>
```

O **más simple: Manual Choreography** (recomendado para comenzar)

**Tareas:**
- [ ] Definir SAGAs por flujo (PatientRegistration, CitaReasignacion, etc.)
- [ ] Crear Event types para cada saga step
- [ ] Implementar compensating transactions
- [ ] Crear SagaCoordinator (centraliza lógica SAGA)
- [ ] Testing: simular fallo en Step 2 → verificar rollback
- [ ] Testing: simular fallo en Step 3 → verificar retry (no rollback)
- [ ] Documentar SAGAs en `SAGA_PATTERN.md`
- [ ] Monitoring de SAGAs (dashboard que muestre qué salió mal)

**Deliverables:**
- SAGAs implementadas
- Compensating transactions operacionales
- Testing de fallos
- Documentación de SAGAs

**Riesgo:** ALTO (lógica transaccional compleja)

---

### Fase 5️⃣: BD Persistente - Insforge/PostgreSQL (3-4 días)

**Objetivo:** Migrar de H2 (en memoria) a PostgreSQL persistente

**Pasos:**

1. **Crear perfiles Spring**

```yaml
# application.yml (base por defecto)
spring:
  profiles:
    active: h2  # Development

---
# application-h2.yml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: create-drop

---
# application-postgres.yml
spring:
  datasource:
    url: jdbc:postgresql://insforge:5432/saludrednorte
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    driver-class-name: org.postgresql.Driver
  jpa:
    database-platform: org.hibernate.dialect.PostgreSQL10Dialect
    hibernate:
      ddl-auto: validate  # No generar automáticamente
  flyway:
    enabled: true
    locations: classpath:db/migration
```

2. **Crear migraciones Flyway**

```
ms-gestionpacientes/src/main/resources/db/migration/
├── V1__initial_paciente_schema.sql
├── V2__initial_lista_espera_schema.sql
└── V3__add_audit_columns.sql

ms-optimizacion/src/main/resources/db/migration/
├── V1__initial_cita_schema.sql
├── V2__initial_medico_horario_schema.sql
└── V3__create_audit_log.sql

ms-notificaciones/src/main/resources/db/migration/
├── V1__initial_notificacion_schema.sql
├── V2__add_retry_attempts.sql
└── V3__create_notification_audit.sql
```

3. **Ejemplo: V1__initial_paciente_schema.sql**

```sql
-- ms-gestionpacientes

CREATE TABLE paciente (
    id BIGSERIAL PRIMARY KEY,
    dni VARCHAR(20) UNIQUE NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    telefono VARCHAR(20),
    fecha_nacimiento DATE,
    genero VARCHAR(20),
    estado_civil VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP,
    updated_by VARCHAR(100),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(100),
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE lista_espera (
    id BIGSERIAL PRIMARY KEY,
    paciente_id BIGINT NOT NULL REFERENCES paciente(id),
    especialidad VARCHAR(100) NOT NULL,
    urgencia INT DEFAULT 1,  -- 1=normal, 2=prioritario, 3=emergencia
    fecha_solicitud TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    estado VARCHAR(20) DEFAULT 'PENDIENTE',  -- PENDIENTE, ASIGNADA, CANCELADA
    observaciones TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_paciente_dni ON paciente(dni);
CREATE INDEX idx_lista_espera_estado ON lista_espera(estado);
CREATE INDEX idx_lista_espera_urgencia ON lista_espera(urgencia);
```

4. **Configurar environment variables**

```bash
# config/local-insforge.env
DB_USERNAME=saludrednorte_user
DB_PASSWORD=your_secure_password
DB_URL=jdbc:postgresql://insforge.company.com:5432/saludrednorte
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
EUREKA_SERVER=http://localhost:8761
SPRING_PROFILES_ACTIVE=postgres
```

5. **Docker Compose actualizado**

```yaml
version: '3.8'
services:
  postgres:
    image: postgres:15
    ports:
      - "5432:5432"
    environment:
      POSTGRES_DB: saludrednorte
      POSTGRES_USER: saludrednorte_user
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes:
      - postgres_data:/var/lib/postgresql/data
      
  kafka:
    # ... (como en Fase 3)
    
  eureka:
    image: saludrednorte/eureka-server:latest
    ports:
      - "8761:8761"
      
  ms-pacientes:
    depends_on:
      - postgres
      - eureka
    ports:
      - "8083:8083"
    environment:
      SPRING_PROFILES_ACTIVE: postgres
      DB_USERNAME: ${DB_USERNAME}
      DB_PASSWORD: ${DB_PASSWORD}
      # ... más variables

volumes:
  postgres_data:
```

**Tareas:**
- [ ] Crear perfiles Spring (h2, postgres)
- [ ] Crear migraciones Flyway en cada MS
- [ ] Configurar datasource PostgreSQL
- [ ] Actualizar docker-compose.yml
- [ ] Testing con H2 (desarrollo)
- [ ] Testing con PostgreSQL (preproducción)
- [ ] Migrar datos existentes (si hay)
- [ ] Realizar backup de datos

**Deliverables:**
- Perfiles Spring configurados
- Migraciones creadas y testeadas
- docker-compose.yml actualizado
- BD persistente operacional

**Riesgo:** BAJO (bien documentado, reversible)

---

### Fase 6️⃣: Documentación & Deployment (2-3 días)

**Objetivo:** Documentar la nueva arquitectura y preparar para producción

**Tareas:**
- [ ] Actualizar `ARCHITECTURE.md`
- [ ] Crear `EVENTOS.md` (catálogo de eventos de negocio)
- [ ] Crear `SAGA_PATTERN.md` (explicación detallada de SAGAs)
- [ ] Crear `DEPLOYMENT.md` (guía de despliegue)
- [ ] AsyncAPI spec (.yaml) para eventos
- [ ] Diagramas C4 (Context, Container, Component)
- [ ] Guía de troubleshooting
- [ ] Runbook de operaciones (monitoreo, alertas)
- [ ] SLOs/SLIs (Service Level Objectives/Indicators)

**Ejemplo: EVENTOS.md**

```markdown
# Catálogo de Eventos - RedNorte

## patient.registered
**Emitido por:** ms-gestionpacientes  
**Momento:** Cuando un paciente se registra  
**Schema:**
```json
{
  "patientId": 123,
  "nombre": "Juan Pérez",
  "email": "juan@example.com",
  "dni": "12345678A",
  "specialidad_requerida": "Cardiología",
  "timestamp": "2026-05-27T10:30:00Z"
}
```

**Suscriptores:**
- ms-optimizacion: Asigna cita automáticamente
- ms-notificaciones: Envía bienvenida
- AuditService: Registra en audit_log

---
```

**Tareas adicionales:**
- [ ] Setup de logging centralizado (ELK Stack o similar)
- [ ] Setup de monitoreo (Prometheus + Grafana)
- [ ] Setup de alertas (PagerDuty, Opsgenie)
- [ ] Revisión de seguridad (OWASP Top 10)
- [ ] Pruebas de carga (JMeter, K6)
- [ ] Pruebas de chaos engineering (Gremlin)
- [ ] Documentación para devops (runbooks)

**Deliverables:**
- Documentación completa
- Diagrams actualizados
- Guías de operación
- Playbooks de troubleshooting

**Riesgo:** BAJO

---

## 6️⃣ ESTRUCTURA DE CARPETAS PROPUESTA

### Nodo: ms-gestionpacientes

```
ms-gestionpacientes/
│
├── pom.xml                                 # ADD: spring-kafka
│
├── src/main/java/com/saludrednorte/ms_listas_espera/
│
├── controller/
│   └── PacienteController.java             # REST API (sin cambios principales)
│
├── service/
│   ├── PacienteService.java                # Business logic (CAMBIAR: agreg event publishing)
│   └── ListaEsperaService.java             # Business logic
│
├── event/                                  # **NEW FOLDER**
│   ├── PatientRegisteredEvent.java         # Event DTO
│   ├── PatientUpdatedEvent.java
│   ├── PatientDeletedEvent.java
│   ├── PatientEventPublisher.java          # **NEW** Publica eventos
│   └── PatientEventPayload.java            # Helper para serializar
│
├── listener/                               # (vacío por ahora)
│
├── config/
│   ├── JpaConfig.java
│   ├── KafkaProducerConfig.java            # **NEW** Configuración Kafka
│   └── KafkaTopicsConfig.java              # **NEW** Tópicos Kafka
│
├── entity/
│   ├── Paciente.java                       # Entity JPA (ADD audit fields)
│   └── ListaEspera.java                    # Entity JPA
│
├── repository/
│   ├── PacienteRepository.java
│   └── ListaEsperaRepository.java
│
├── dto/
│   ├── PacienteDTO.java
│   ├── CreatePacienteRequest.java
│   ├── UpdatePacienteRequest.java
│   └── PacienteResponse.java
│
├── exception/
│   ├── PacienteYaExisteException.java
│   └── PacienteNoEncontradoException.java
│
├── MsListasEsperaApplication.java
│
└── src/main/resources/
    ├── application.yml                     # Base config
    ├── application-h2.yml                  # **NEW** Desarrollo (en memoria)
    ├── application-postgres.yml            # **NEW** Producción (Insforge)
    │
    ├── db/migration/
    │   ├── V1__initial_paciente_schema.sql          # **NEW**
    │   └── V2__initial_lista_espera_schema.sql      # **NEW**
    │
    └── logback-spring.xml                  # Logging config
```

### Nodo: ms-optimizacion

```
ms-optimizacion/
│
├── src/main/java/com/saludrednorte/ms_optimizacion/
│
├── controller/
│   └── OptimizacionController.java
│
├── service/
│   ├── OptimizacionService.java            # Business logic
│   ├── CitaService.java
│   ├── MedicoService.java
│   └── EstrategiaOptimizacion*Service.java # Strategies (FIFO, PorGravedad, etc.)
│
├── event/                                  # **NEW FOLDER**
│   ├── AppointmentAssignedEvent.java
│   ├── AppointmentRescheduledEvent.java
│   ├── AppointmentCancelledEvent.java
│   ├── AppointmentEventPublisher.java
│   └── AppointmentEventPayload.java
│
├── listener/                               # **NEW FOLDER**
│   └── PatientEventListeners.java          # Escucha patient.registered
│
├── config/
│   ├── KafkaProducerConfig.java            # **NEW**
│   ├── KafkaConsumerConfig.java            # **NEW** (consumer de patient.*)
│   ├── KafkaTopicsConfig.java              # **NEW**
│   └── FeignConfig.java
│
├── entity/
│   ├── Cita.java                           # ADD audit fields
│   ├── Medico.java
│   └── Horario.java
│
├── repository/
│   ├── CitaRepository.java
│   ├── MedicoRepository.java
│   └── HorarioRepository.java
│
├── dto/
│   ├── CitaDTO.java
│   ├── MedicoDTO.java
│   └── HorarioDTO.java
│
├── client/
│   └── ListaEsperaClient.java              # Feign (mantener para consultas)
│
└── src/main/resources/
    ├── application.yml
    ├── application-h2.yml                  # **NEW**
    ├── application-postgres.yml            # **NEW**
    └── db/migration/
        ├── V1__initial_cita_schema.sql     # **NEW**
        └── V2__initial_medico_horario_schema.sql # **NEW**
```

### Nodo: ms-notificaciones

```
ms-notificaciones/
│
├── src/main/java/com/saludrednorte/ms_notificaciones/
│
├── service/
│   ├── NotificacionService.java            # Business logic
│   ├── NotificacionEnvioService.java       # Scheduler + envío real
│   └── UHDNotificationAdapter.java         # Adapter para envío externo
│
├── listener/                               # **NEW FOLDER**
│   ├── PatientEventListeners.java          # Escucha patient.*
│   └── AppointmentEventListeners.java      # Escucha appointment.*
│
├── publisher/                              # **NEW FOLDER**
│   └── NotificationEventPublisher.java     # Publica notification.*
│
├── config/
│   ├── KafkaConsumerConfig.java            # **NEW**
│   └── KafkaTopicsConfig.java              # **NEW**
│
├── entity/
│   ├── Notificacion.java                   # ADD retry fields
│   ├── NotificacionIntento.java            # Track de intentos
│   └── NotificacionCanalEnvio.java
│
├── dto/
│   ├── PatientRegisteredEvent.java         # Event from Kafka
│   ├── AppointmentAssignedEvent.java
│   └── NotificacionRequest.java
│
├── enums/
│   ├── TipoCanal.java                      # EMAIL, SMS, PUSH
│   ├── TipoNotificacion.java               # PACIENTE_REGISTRADO, CITA_ASIGNADA, etc
│   └── EstadoNotificacion.java             # PENDIENTE, ENVIADA, FALLIDA, CANCELADA
│
└── src/main/resources/
    ├── application.yml
    ├── application-h2.yml                  # **NEW**
    ├── application-postgres.yml            # **NEW**
    └── db/migration/
        ├── V1__initial_notificacion_schema.sql # **NEW**
        └── V2__add_retry_tracking.sql      # **NEW**
```

### Nodo NUEVO: AuditService (Microservicio nuevo)

```
audit-service/
│
├── pom.xml                                 # spring-kafka, spring-data-jpa
│
├── src/main/java/com/saludrednorte/audit/
│
├── listener/
│   └── AuditEventListener.java             # Escucha TODOS los eventos
│
├── service/
│   └── AuditService.java                   # Persiste en BD
│
├── entity/
│   ├── AuditEvent.java
│   ├── AuditAccess.java
│   └── AuditChange.java
│
├── repository/
│   ├── AuditEventRepository.java
│   └── AuditAccessRepository.java
│
├── config/
│   └── KafkaConsumerConfig.java
│
└── src/main/resources/
    ├── application.yml
    └── db/migration/
        ├── V1__initial_audit_schema.sql
        └── V2__add_compliance_fields.sql
```

---

## 7️⃣ DIAGRAMAS DE FLUJO

### Flujo 1: Registrar Paciente (Normal)

```
┌─────────────────────────────────────────────────────────────────┐
│ [1] Cliente: POST /api/pacientes                               │
│     Body: { dni, nombre, email, especialidad_requerida }        │
└────────────────────┬────────────────────────────────────────────┘
                     │ HTTP Request
                     ↓
            ┌────────────────────────┐
            │  API Gateway (8080)    │
            │  Route: /api/pacientes │
            └────────────┬───────────┘
                         │ Forward
                         ↓
─────────────────────────────────────────────────────────────────
│ ms-gestionpacientes (8083)                              [2]    │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  POST /pacientes                                               │
│  ├─ Valida DNI no existe                                       │
│  ├─ Crea Paciente entity                                       │
│  ├─ Guarda en PostgreSQL ✅                                    │
│  ├─ Guarda evento en event_store ✅                           │
│  │                                                             │
│  └─ EMITE EVENTO: patient.registered                          │
│     └─ Asíncrono (no espera)                                  │
│     └─ Kafka Topic: "patient.registered"                      │
│        {                                                       │
│          "patientId": 123,                                    │
│          "nombre": "Juan",                                    │
│          "email": "juan@api.com",                            │
│          "especialidad_requerida": "Cardiología",           │
│          "timestamp": "2026-05-27T10:35:00Z"                │
│        }                                                       │
│                                                             │
│  Retorna: 201 CREATED                                        │
│           Location: /api/pacientes/123                       │
│           Body: { id: 123, dni, nombre, ... }               │
└──┬────────────────────────────────────────────────────────────┘
   │
   └──────────────────────────────────→ Cliente recibe respuesta (10ms aprox)
                                       Usuario ve "Paciente registrado"
─────────────────────────────────────────────────────────────────
│ Kafka Topic: "patient.registered" [3]                        │
│                                                              │
│ El sistema continúa asíncrónicamente:                      │
│                                                              │
│ ┌─────────────────────────────────────────────────────────┐ │
│ │ Suscriptor 1: ms-optimizacion (group: optim-group)     │ │
│ ├─────────────────────────────────────────────────────────┤ │
│ │ @KafkaListener(topics = "patient.registered")          │ │
│ │                                                         │ │
│ │ ├─ Recibe evento paciente 123                          │ │
│ │ ├─ Consulta lista_espera (Feign) → GET /lista-espera  │ │
│ │ ├─ Obtiene config de horarios para Cardiología        │ │
│ │ ├─ Aplica estrategia FIFO/GRAVEDAD/...                │ │
│ │ ├─ Encuentra slot libre: 2026-05-29 09:00 (Dr. López) │ │
│ │ ├─ Crea Cita en PostgreSQL ✅                         │ │
│ │ ├─ Guarda evento en event_store ✅                   │ │
│ │ │                                                     │ │
│ │ └─ EMITE: appointment.assigned                        │ │
│ │    Kafka Topic: "appointment.assigned"               │ │
│ │    {                                                 │ │
│ │      "appointmentId": 456,                          │ │
│ │      "patientId": 123,                              │ │
│ │      "fecha": "2026-05-29",                          │ │
│ │      "hora": "09:00",                                │ │
│ │      "medico": "Dr. López"                           │ │
│ │    }                                                 │ │
│ └─────────────────────────────────────────────────────────┘ │
│                                                              │
│ ┌─────────────────────────────────────────────────────────┐ │
│ │ Suscriptor 2: ms-notificaciones (group: notif-group)  │ │
│ ├─────────────────────────────────────────────────────────┤ │
│ │ @KafkaListener(topics = "patient.registered")          │ │
│ │                                                         │ │
│ │ ├─ Recibe evento paciente 123                          │ │
│ │ ├─ Crea Notificacion:                                  │ │
│ │ │  - Tipo: PACIENTE_REGISTRADO                        │ │
│ │ │  - Canal: EMAIL                                     │ │
│ │ │  - Estado: PENDIENTE                                │ │
│ │ │  - Scheduled para 5min después                      │ │
│ │ ├─ Guarda en PostgreSQL ✅                            │ │
│ │ └─ No emite eventos (solo consume)                    │ │
│ └─────────────────────────────────────────────────────────┘ │
│                                                              │
│ ┌─────────────────────────────────────────────────────────┐ │
│ │ Suscriptor 3: AuditService (group: audit-group)       │ │
│ ├─────────────────────────────────────────────────────────┤ │
│ │ @KafkaListener(topics = "patient.registered")          │ │
│ │                                                         │ │
│ │ ├─ Recibe evento paciente 123                          │ │
│ │ ├─ Crea audit_event:                                   │ │
│ │ │  - action: CREATE                                   │ │
│ │ │  - resource_type: PATIENT                           │ │
│ │ │  - resource_id: 123                                 │ │
│ │ │  - timestamp: 2026-05-27T10:35:00Z                  │ │
│ │ │  - actor: API                                       │ │
│ │ ├─ Guarda en PostgreSQL ✅                            │ │
│ │ └─ No emite eventos (audit trail)                     │ │
│ └─────────────────────────────────────────────────────────┘ │
│                                                              │
│ Luego: appointment.assigned es escuchado por ms-notificaciones
│ ├─ Crea Notificacion: CITA_ASIGNADA                        │
│ ├─ scheduler la envía como SMS/EMAIL inmediatamente        │
│ ├─ Guarda comprobante de envío                             │
│ └─ EMITE: notification.sent                                │
│    └─ AuditService lo registra                             │
└─────────────────────────────────────────────────────────────

RESULTADO FINAL (después de ~200-500ms):
✅ Paciente registrado en BD
✅ Cita asignada automáticamente
✅ Paciente notificado (2x: registro + asignación cita)
✅ Auditoría completa
✅ API retornó 201 al cliente en 10ms
✅ Sistema robusto: si AuditService cae, no afecta a paciente
```

### Flujo 2: Registrar Paciente (Con Fallo)

```
┌──────────────────────────────────────────────────────────────┐
│ Escenario: ms-optimizacion DOWN cuando se registra paciente  │
└──────────────────────────────────────────────────────────────┘

[1] ms-gestionpacientes:
    ├─ Guarda Paciente ✅
    └─ EMITE: patient.registered → Kafka

[2] ms-optimizacion (DOWN):
    ├─ No puede procesar mensaje
    └─ Kafka replica en Dead Letter Queue (DLQ)

[3] Después de 5-10min, ms-optimizacion se RECUPERA:
    ├─ Kafka automáticamente RETRANSMITE desde DLQ
    ├─ ms-optimizacion ahora SÍ puede procesar
    ├─ Crea Cita retroactivamente
    ├─ EMITE: appointment.assigned
    └─ ms-notificaciones recibe → envía notificación

[4] RESULTADO:
    ✅ Paciente registrado (sin demora)
    ✅ Cita asignada (con pequeño retraso, pero garantizado)
    ✅ Auditoría de todo
    ✅ Transacción distribuida GARANTIZADA
    
    SI NO HUBIERA SIDO EVENT-DRIVEN:
    ❌ Paciente registrado = FALSO
    ❌ Cliente recibe HTTP 500
    ❌ Experiencia terrible
```

---

## 8️⃣ DECISIONES ARQUITECTÓNICAS Y JUSTIFICACIÓN

### 1. **¿Por qué Kafka en lugar de RabbitMQ?**

| Aspecto | Kafka | RabbitMQ |
|---------|-------|----------|
| **Throughput** | ⬆️ Alta (millions msg/s) | 🟡 Media (100k msg/s) |
| **Complejidad** | 🟡 Zookeeper + Brokers | ✅ Simple |
| **Retencion** | ✅ Meses/años | ❌ Temporal |
| **Event Sourcing** | ✅ Perfecto | 🟡 No ideal |
| **Auditoría** | ✅ Replay de eventos | 🟡 Más difícil |
| **Compliance Health** | ✅ Better (HIPAA-ready) | 🟡 Estándar |
| **Coste Infraestructura** | 🟡 Mayor | ✅ Menor |

**Decisión Final: KAFKA**
- Dominio de salud requiere auditoría completa (HIPAA/RGPD)
- Retencion de eventos permite compliance
- Future-proof para Event Sourcing + CQRS

---

### 2. **¿Choreography vs Orchestration SAGA?**

#### Choreography (Sin Orquestador Central)
```
Cada servicio reacciona a eventos y emite otros:

ms-gestionpacientes emite patient.registered
    ↓
ms-optimizacion escucha y emite appointment.assigned
    ↓
ms-notificaciones escucha y emite notification.sent
    ↓
AuditService escucha todo

Ventajas:
✅ Bajo acoplamiento
✅ Escalable
✅ Fácil de testear en aislamiento

Desventajas:
❌ Difícil de debuggear (flujo está distribuido)
❌ Sin visibilidad global de SAGA
❌ Lógica compensación esparcida
```

#### Orchestration (Con SagaOrchestrator Central)
```
SagaOrchestrator central orquesta todo:

SagaOrchestrator recibe registrar-paciente
    ├─ Llama ms-gestionpacientes.registrar()
    ├─ Espera respuesta
    ├─ Si OK → Llama ms-optimizacion.asignarCita()
    ├─ Espera respuesta
    ├─ Si OK → Llama ms-notificaciones.enviar()
    ├─ Si alguno falla → CompensateAll()

Ventajas:
✅ Lógica SAGA centralizada
✅ Visibilidad global
✅ Fácil debuggear

Desventajas:
❌ Alto acoplamiento (orquestador conoce todos los servicios)
❌ SPOF (si orquestador cae, SAGAs bloquean)
❌ Menos escalable
```

**Decisión Final: CHOREOGRAPHY PURO + Monitoring**
- Usa eventos de Kafka
- Cada servicio independiente
- Agrega monitoring/tracing para ver SAGA global (OpenTelemetry)
- Más resiliente y escalable para sistema de salud

---

### 3. **¿Feign vs REST Template vs Webclient?**

**MANTENER FEIGN PERO SOLO PARA:**
- Consultas de lectura (GET)
- Información de referencia (maestros)
- Cuando latencia no es crítica

**CAMBIAR A KAFKA PARA:**
- Creación de datos (POST)
- Modificación de datos (PUT)
- Eliminación de datos (DELETE)
- Cualquier operación que requiera transacción distribuida

**Justificación:**
- Feign síncrono = acoplamiento fuerte + fallos en cascada
- Kafka asíncrono = desacoplamiento + resiliencia

---

### 4. **¿Event Sourcing o Tradicional BD?**

**Propuesta Hybrid:**
```
BD Tradicional (PostgreSQL):
  ├─ Tablas de entidades (paciente, cita, notificacion)
  └─ Índices normalizados para queries rápidas

+

Event Store (Kafka):
  ├─ Inmutable log de todos los eventos
  ├─ Usado para auditoría + replay
  ├─ Permite reconstruir estado histórico
  └─ Cumple requisitos regulatorios

VENTAJA:
✅ BD tradicional rápida para queries
✅ Event Store para auditoría perfecta
✅ Lo mejor de ambos mundos
```

---

### 5. **¿Cuándo rollback en SAGA?**

**PATRÓN PROPUESTO:**

```
1. Paciente registrado → COMMITEABLE (es la raíz)

2. Cita asignada → SI FALLA → Rollback paciente
   Compensante: eliminar_paciente()
   
3. Notificación (email/sms) → SI FALLA → Reintentos (No rollback)
   Razón: Notificación es non-critical
   Retries automáticos con DLQ

4. Auditoría → Nunca rollback
   Es observador pasivo
```

---

## 9️⃣ TIMELINE ESTIMADO

| Fase | Descripción | Duración | Dependencias |
|------|-------------|----------|--------------|
| 1️⃣ | Auditoría & Análisis | 2-3 días | N/A |
| 2️⃣ | Refactorización Dominios | 5-7 días | Fase 1 |
| 3️⃣ | Implementar Kafka | 4-5 días | Fase 2 |
| 4️⃣ | SAGA Pattern | 5-7 días | Fase 3 |
| 5️⃣ | BD Persistente | 3-4 días | Fase 2 |
| 6️⃣ | Documentación & Deploy | 2-3 días | Fase 4, 5 |
| **TOTAL** | | **22-29 días** | ~4-5 semanas |

**Modo Paralelo (recomendado):**
- Fases 2 + 5 en paralelo (Refacto + BD)
- Luego Fase 3 (Kafka)
- Luego Fase 4 (SAGA)
- Finalmente Fase 6 (Docs)

**Timeline Optimizado: 18-21 días**

---

## 🔟 CHECKLIST DE IMPLEMENTACIÓN COMPLETO

### ✅ Fase 1: Auditoría
- [ ] Documentar flujos actuales en C4 diagrams
- [ ] Mapear eventos de negocio por dominio
- [ ] Listar transacciones distribuidas críticas
- [ ] Identificar puntos de fallo (SPOF)
- [ ] Validar requisitos HIPAA/RGPD

### ✅ Fase 2: Refactorización
- [ ] Crear clases Event DTOs (PatientRegisteredEvent, etc.)
- [ ] Extraer `PacienteEventPublisher` de `PacienteService`
- [ ] Extraer `AppointmentEventPublisher` de `OptimizacionService`
- [ ] Crear `NotificationEventListener` en ms-notificaciones
- [ ] Remover lógica de notificación directa
- [ ] Añadir fields de auditoría a entities (created_by, created_at, etc.)

### ✅ Fase 3: Message Broker
- [ ] Agregar spring-kafka a todos los pom.xml
- [ ] Crear `KafkaProducerConfig` en ms-gestionpacientes y ms-optimizacion
- [ ] Crear `KafkaConsumerConfig` en ms-notificaciones, ms-optimizacion, audit-service
- [ ] Crear `KafkaTopicsConfig` centralizado
- [ ] Implementar @KafkaListener en cada servicio
- [ ] Configurar Dead Letter Queue (DLQ) para failed messages
- [ ] Configurar retry policy (exponential backoff)
- [ ] Testing: publicar evento → verificar que listener reacciona

### ✅ Fase 4: SAGA Pattern
- [ ] Definir SAGAs por flujo (PatientRegistration, CitaReasignacion)
- [ ] Crear Event types para cada saga step
- [ ] Implementar compensating transactions
- [ ] Testing: simular fallo en Step 2 → verificar rollback
- [ ] Testing: simular fallo en Step 3 → verificar retry
- [ ] Documentar SAGAs en `SAGA_PATTERN.md`
- [ ] Crear dashboard para monitoreo de SAGAs

### ✅ Fase 5: BD Persistente
- [ ] Crear perfiles Spring (h2, postgres)
- [ ] Crear `application-postgres.yml` en cada MS
- [ ] Agregar driver PostgreSQL (ya existe en pom.xml)
- [ ] Crear migraciones Flyway en cada MS (`V1__initial_schema.sql`)
- [ ] Testing con H2 (desarrollo)
- [ ] Testing con PostgreSQL (preproducción)
- [ ] Actualizar docker-compose.yml con PostgreSQL + Kafka
- [ ] Documentar variables de entorno (Insforge credentials)

### ✅ Fase 6: Documentación
- [ ] Actualizar `ARCHITECTURE.md`
- [ ] Crear `EVENTOS.md` (catálogo de eventos)
- [ ] Crear `SAGA_PATTERN.md` (explicación de SAGAs)
- [ ] Crear `DEPLOYMENT.md` (guía de despliegue)
- [ ] AsyncAPI spec (.yaml) para eventos
- [ ] Diagramas C4 (Context, Container, Component)
- [ ] Guía de troubleshooting
- [ ] Runbook de operaciones (monitoreo, alertas)
- [ ] SLOs/SLIs documentados

---

## 1️⃣1️⃣ PRÓXIMOS PASOS INMEDIATOS

### ✅ Esta Semana:
1. **Reunión técnica de alineación**: Presentar este rediseño al equipo
2. **Aprobación de arquitectura**: Cierre de decisiones clave
3. **Iniciar Fase 1**: Comenzar auditoría detallada
4. **Setup de repo**: Crear branches para cada fase

### ✅ Semana 2-3:
1. **Implementar Fase 2**: Refactorización de dominios
2. **Setup infraestructura**: Kafka local + PostgreSQL
3. **Pull requests iniciales**: Event classes + config

### ✅ Semana 4:
1. **Fase 3 completa**: Kafka producers/consumers working
2. **Fase 4 comienza**: SAGA Pattern
3. **Testing**: Event-driven flows testeados

### ✅ Semana 5:
1. **Documentación final**: Todos los docs actualizados
2. **Preparación deployment**: Docker compose listo
3. **UAT**: Testing en preproducción

---

## 1️⃣2️⃣ RIESGOS Y MITIGACIÓN

| Riesgo | Probabilidad | Impacto | Mitigación |
|--------|-------------|--------|----------|
| Kafka learning curve | Media | Alto | Workshop previo, arquitecto dedicado |
| Retrasos en Fase 3 | Media | Medio | Iniciar temprano, pair programming |
| Data inconsistency con eventos | Baja | Crítico | Testing exhaustivo de SAGAs |
| Downtime al migrar H2 → PostgreSQL | Baja | Alto | Migration script, rollback plan |
| Performance degradation | Baja | Medio | Load testing en Fase 6 |
| Compliance no cumplido | Baja | Crítico | Auditoría legal en Fase 1 |

---

## CONCLUSIÓN

Este rediseño transforma tu sistema de **monolítico desconectado** a **Event-Driven Architecture resiliente**:

✅ **Desacoplado**: Servicios independientes via eventos Kafka  
✅ **Resiliente**: Circuit breaker, retries, DLQ  
✅ **Escalable**: PostgreSQL + Kafka horizontal scaling  
✅ **Auditable**: Event Sourcing completo + audit logs  
✅ **Compliant**: HIPAA/RGPD ready  
✅ **Mantenible**: Responsabilidades claras, fácil agregar features  

---

**Preparado por:** GitHub Copilot  
**Fecha:** 27 de Mayo, 2026  
**Estado:** Listo para implementación

