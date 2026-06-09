# 🚀 QUICK START: Rediseño Arquitectónico

**¿Por dónde empiezo?** Lee esta guía primero.

---

## 📖 Estructura de Documentación

```
Para entender el rediseño (en este orden):

1️⃣ "RESUMEN_EJECUTIVO_REDISEÑO.md"
   └─ Visión general (5 min)
   
2️⃣ "DIAGRAMA VISUAL" (este archivo)
   └─ Entender flujos (5 min)
   
3️⃣ "EVENTOS.md"
   └─ Catálogo detallado de eventos (20 min)
   
4️⃣ "REDISEÑO_ARQUITECTONICO.md"
   └─ Documentación COMPLETA (45 min)

Para implementar:

5️⃣ Fase 1-6 según "REDISEÑO_ARQUITECTONICO.md" (5 semanas)
```

---

## 🎯 El Problema en 30 segundos

**ACTUAL:**
```
ms-gestionpacientes → POST /pacientes
                      └─ Guarda paciente
                      └─ FIN (no notifica) ❌

Paciente jamás es notificado
Cita jamás es asignada
Sistema DESCONECTADO
```

**PROPUESTA:**
```
ms-gestionpacientes → POST /pacientes
                      └─ Guarda paciente
                      └─ EMITE: patient.registered
                      
Kafka Topic: patient.registered
├─ ms-optimizacion: asigna cita automáticamente
├─ ms-notificaciones: envía mail
└─ AuditService: registra todo

RESULTADO: Sistema CONECTADO ✅
```

---

## 🏗️ Arquitectura Propuesta (Visual Simple)

```
┌─────────────────────────────────────────────────────────┐
│ FRONTEND (React + Vite)                                 │
└────────────────────┬────────────────────────────────────┘
                     │ HTTP
                     ↓
        ┌────────────────────────────┐
        │  API Gateway (8080)        │
        └────────────┬───────────────┘
                     │
        ┌────────────┴───────────┐
        ↓                        ↓
  ┌──────────────┐      ┌──────────────┐
  │ Pacientes    │      │ Citas        │
  │ Service      │      │ Service      │
  │ (8083)       │      │ (8084)       │
  │              │      │              │
  │ EMIT: →      │      │ → LISTEN:    │
  │ patient.*    │      │ patient.*    │
  └────┬─────────┘      └──┬───────────┘
       │                    │
       └────────┬───────────┘
                │ Kafka Event Bus
                ↓
  ┌──────────────────────────────────────┐
  │ Notificaciones Service (8085)        │
  │                                      │
  │ LISTEN:                              │
  │ • patient.registered                 │
  │ • appointment.assigned               │
  │                                      │
  │ → SEND: Email, SMS, Push             │
  └──────────────────────────────────────┘
                │
                ├─ PostgreSQL DB
                ├─ Kafka (Message Broker)
                ├─ Eureka (Service Discovery)
                └─ AuditService
```

---

## 📊 Comparativa: Antes vs Después

### ANTES (Con Problemas)
```
Cliente: "Registrar paciente"
     ↓ (10ms)
ms-gestionpacientes guarda
     ↓ (Realiza Feign directo)
Llama ms-notificaciones
     ↓
Si ms-notificaciones cae → TODO FALLA
     ↓
Datos en H2 (memoria)
     ↓
Sin auditoría
     ↓
❌ SISTEMA DISFUNCIONAL
```

### DESPUÉS (Solución)
```
Cliente: "Registrar paciente"
     ↓ (10ms)
ms-gestionpacientes guarda
     ↓ (Emite evento Kafka)
Kafka distribuye asíncrono
     ├─ Si ms-notificaciones cae → reintentos automáticos
     ├─ Si AuditService cae → no bloquea ni afecta
     └─ Si algo falla → guardar en Dead Letter Queue
          ↓ (cuando se recupera)
          └─ Reintenta automáticamente
     ↓
Datos en PostgreSQL (persistente)
     ↓
Auditoría completa de todo
     ↓
✅ SISTEMA RESILIENTE
```

---

## 🔄 Flujo Completo: Registrar Paciente

```
┌──────────────────────────────────────────────────────┐
│ Step 1: Cliente hace REST call                       │
│ POST /api/pacientes                                  │
│ {dni, nombre, email, especialidad_requerida}         │
└──────────────────┬───────────────────────────────────┘
                   │ (10ms)
┌──────────────────▼───────────────────────────────────┐
│ Step 2: API Gateway                                  │
│ Route a /api/pacientes → ms-gestionpacientes        │
└──────────────────┬───────────────────────────────────┘
                   │ (5ms)
┌──────────────────▼───────────────────────────────────┐
│ Step 3: ms-gestionpacientes                          │
│ • Valida DNI no existe                               │
│ • Guarda Paciente en PostgreSQL ✅                   │
│ • EMITE: patient.registered → Kafka                  │
│ • RETORNA: 201 CREATED al cliente                    │
│                                                      │
│ ⚠️ CLIENTE YA TIENE RESPUESTA (15ms)                │
│    Usuario ve "Paciente registrado"                  │
│    El sistema CONTINÚA ASÍNCRONO...                  │
└──────────────────┬───────────────────────────────────┘
                   │ (Ahora en paralelo)
       ┌───────────┼────────────┬─────────────┐
       ↓           ↓            ↓             ↓
    ┌──────┐   ┌───────┐   ┌──────────┐  ┌────────┐
    │Optim │   │Notif  │   │Audit     │  │(otros) │
    │(20ms)│   │(20ms) │   │(20ms)    │  │        │
    └──┬───┘   └───┬───┘   └────┬─────┘  └────────┘
       │           │            │
   Asigna      Envía mail    Registra
   cita        bienvenida    evento

       ↓           ↓            ↓
   ┌──────────────────────────────────┐
   │ All data in PostgreSQL (saved)   │
   │ All events in Kafka (audit log)  │
   └──────────────────────────────────┘

┌──────────────────────────────────────────────────────┐
│ Step 4: appointment.assigned (otro evento Kafka)     │
│   ↓                                                   │
│ ms-notificaciones escucha                            │
│   ├─ Crea notificación "CITA_ASIGNADA"              │
│   ├─ ENVÍA: SMS + Email inmediatamente              │
│   └─ EMITE: notification.sent                        │
│                                                      │
│ AuditService escucha                                 │
│   └─ Registra asignación de cita                     │
└──────────────────────────────────────────────────────┘

✅ RESULTADO FINAL (200ms después):
   • Paciente registrado en BD
   • Cita asignada automáticamente
   • Paciente notificado (2 mensajes)
   • Auditoría completa
   • Si algo falló → reintentos automáticos
```

---

## 💾 BD: Antes vs Después

### ANTES: H2 (En Memoria)

```sql
-- Datos LOCALES a cada MS
-- ms-gestionpacientes tiene su H2
-- ms-optimizacion tiene su H2
-- ms-notificaciones tiene su H2

CREATE TABLE paciente (
    id BIGINT,
    nombre VARCHAR(100),
    ...
);

❌ Se pierden al reiniciar el servicio
❌ Imposible auditoría
❌ No hay backup
```

### DESPUÉS: PostgreSQL (Persistente)

```sql
-- Base de datos COMPARTIDA (o por dominio con migración)
postgres://insforge:5432/saludrednorte

CREATE TABLE paciente (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100),
    created_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP,
    is_deleted BOOLEAN,
    ...
);

CREATE TABLE cita (
    id BIGSERIAL PRIMARY KEY,
    paciente_id BIGINT REFERENCES paciente(id),
    ...
);

CREATE TABLE notificacion (
    id BIGSERIAL PRIMARY KEY,
    paciente_id BIGINT REFERENCES paciente(id),
    tipo VARCHAR(50),
    estado VARCHAR(20),
    ...
);

CREATE TABLE audit_log (
    id BIGSERIAL PRIMARY KEY,
    event_type VARCHAR(100),
    resource_type VARCHAR(50),
    resource_id BIGINT,
    action VARCHAR(20),
    timestamp TIMESTAMP,
    actor VARCHAR(100),
    ...
);

✅ Datos persistentes
✅ Auditoría completa
✅ Backups posibles
✅ HIPAA/RGPD compliant
```

---

## 🎯 ¿Por Dónde Empiezo? (Recomendación)

### Opción A: MVP Rápido (2 semanas)
Si necesitas algo funcionando YA:

```
Semana 1:
├─ Refactorizar código (crear Event classes)
└─ Integrar Kafka básico

Semana 2:
├─ Implementar listeners
└─ Testing básico

RESULTADO: Event-Driven pero sin SAGA ni BD persistente
PROS: Rápido, desacopla servicios
CONTRAS: Aún en H2, transacciones manuales
```

### Opción B: Punto Medio (4 semanas) ⭐ RECOMENDADO
Balance entre riesgos y beneficios:

```
Semana 1: Refactorizar + Kafka
Semana 2: Listeners + Dead Letter Queue
Semana 3: Migrar a PostgreSQL (Flyway)
Semana 4: Testing + Documentación

RESULTADO: Event-Driven + BD Persistente
PROS: Buena arquitectura, BD segura, reasonable timeline
CONTRAS: Sin SAGA Pattern aún (agregarlo después)
```

### Opción C: Completo (5-6 semanas)
Todo incluido:

```
Semana 1-2: Todo lo de Opción B
Semana 3-4: SAGA Pattern + Compensating transactions
Semana 5-6: Documentación + Monitoreo

RESULTADO: Arquitectura Enterprise completa
PROS: Todo implementado correctamente
CONTRAS: Timeline más largo, más complejidad
```

---

## 📝 Cambios Clave Resumidos

### En ms-gestionpacientes (Cambio mínimo)

```java
// AGREGAR ESTA CLASE:
@Component
public class PacienteEventPublisher {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    
    public void publishPatientRegistered(PatientRegisteredEvent e) {
        kafkaTemplate.send("patient.registered", toJson(e));
    }
}

// MODIFICAR ESTA LÍNEA EN PacienteService:
public Paciente registrarPaciente(Paciente p) {
    Paciente saved = pacienteRepository.save(p);
    eventPublisher.publishPatientRegistered(/* ... */);  // ← NEW
    return saved;
}
```

### En ms-notificaciones (Cambio pequeño)

```java
// AGREGAR ESTA CLASE:
@Component
public class NotificacionEventListeners {
    
    @KafkaListener(topics = "patient.registered", groupId = "notif-group")
    public void onPatientRegistered(String message) {
        // Crea notificación de bienvenida
        Notificacion notif = new Notificacion();
        notif.setTipo("PACIENTE_REGISTRADO");
        // ... enviar en 5 minutos
    }
    
    @KafkaListener(topics = "appointment.assigned", groupId = "notif-group")
    public void onAppointmentAssigned(String message) {
        // Envía notificación de cita asignada
        // ... SMS + Email ahora
    }
}
```

### En ms-optimizacion (Cambio mediano)

```java
// AGREGAR ESTA CLASE:
@Component
public class PatientEventListener {
    
    @KafkaListener(topics = "patient.registered", groupId = "optim-group")
    public void onPatientRegistered(String message) {
        // Asigna cita automáticamente
        // Obtiene lista de espera
        // Busca horario disponible
        // Emite appointment.assigned
    }
}

// AGREGAR PUBLISHER:
@Component
public class AppointmentEventPublisher {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    
    public void publishAppointmentAssigned(AppointmentAssignedEvent e) {
        kafkaTemplate.send("appointment.assigned", toJson(e));
    }
}
```

---

## 🔧 Stack Tecnológico

```
Spring Boot 3.4.1 ✅ (ya está)
├─ Agregar: spring-kafka
├─ Ya está: spring-data-jpa
├─ Ya está: postgresql driver
└─ Agregar: flyway (migrations)

Infraestructura:
├─ Kafka (nuevo)
├─ PostgreSQL (nuevo, pero opcional al inicio)
├─ Eureka (mantener igual)
└─ Docker Compose (actualizar)
```

---

## 📋 Checklist Fase 1 (Primera Semana)

Para comenzar:

- [ ] Leer `RESUMEN_EJECUTIVO_REDISEÑO.md`
- [ ] Leer `EVENTOS.md` (catálogo)
- [ ] Crear clase `PatientRegisteredEvent`
- [ ] Crear clase `AppointmentAssignedEvent`
- [ ] Crear `PacienteEventPublisher` en ms-gestionpacientes
- [ ] Crear `PatientEventListener` en ms-optimizacion
- [ ] Crear `NotificacionEventListeners` en ms-notificaciones
- [ ] Agregar `spring-kafka` a pom.xml
- [ ] Crear `KafkaProducerConfig`
- [ ] Crear `KafkaConsumerConfig`
- [ ] Primer test: enviar evento → escuchar evento

---

## 🚦 Próximos Pasos (Orden Recomendado)

```
AHORA: 
├─ Entender los documentos
├─ Decidir cuál Opción (A, B, C)
└─ Reservar 2-6 semanas en el calendar

SEMANA 1:
├─ Crear Event classes
├─ Crear publishers/listeners
├─ Setup Kafka local
└─ Test básicos

SEMANA 2:
├─ Implementar Dead Letter Queue
├─ Retry policy
├─ Más testing
└─ Code review

SEMANA 3+ (Si elegiste Opción B o C):
├─ Migrations Flyway
├─ PostgreSQL setup
└─ Data migration

SEMANA 4+: Documentación + Deploy
```

---

## 💬 Dudas Comunes

### P: ¿Puedo hacer esto SIN Kafka?
**R:** Sí, usando Feign + agregando circuit breaker, pero Kafka es mejor.

### P: ¿Necesito cambiar H2 a PostgreSQL AHORA?
**R:** No, puedes comenzar con Kafka en H2, luego migrar a PostgreSQL.

### P: ¿Cuánto tiempo toma?
**R:** 2 semanas (MVP) a 5 semanas (Completo) según Opción.

### P: ¿Puedo hacerlo sin downtime?
**R:** Sí, es backward-compatible si lo planeas bien.

### P: ¿Qué pasa con el código actual?
**R:** Se mantiene, solo agregas Event publishing. Gradual refactor.

### P: ¿Quién mantiene la documentación?
**R:** El equipo. Hay template en `EVENTOS.md` para agregar nuevos eventos.

---

## 📚 Documentación Completa

| Documento | Propósito | Duración |
|-----------|----------|----------|
| `RESUMEN_EJECUTIVO_REDISEÑO.md` | Overview ejecutivo | 5 min |
| `EVENTOS.md` | Catálogo detallado de eventos | 20 min |
| `REDISEÑO_ARQUITECTONICO.md` | Documentación COMPLETA | 45 min |
| Este archivo (`QUICK_START.md`) | Guía de inicio | 5 min |

---

## ✅ Conclusión

**Este rediseño convierte tu sistema de:**

```
Desconectado → Conectado
No persistente → Persistente
Sin auditoría → Auditable
Frágil → Resiliente
Difícil de mantener → Fácil de escalar
```

**Recomendación:** 
- Lee primero `RESUMEN_EJECUTIVO_REDISEÑO.md`
- Luego lee `EVENTOS.md`
- Elige Opción B (4 semanas, punto medio)
- Comienza Semana próxima

---

**Documento:** QUICK_START.md  
**Versión:** 1.0  
**Estado:** Listo para empezar

