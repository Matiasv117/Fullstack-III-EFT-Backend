# 📨 Catálogo de Eventos - Sistema RedNorte

**Documento de Referencia para Event-Driven Architecture**

---

## 1️⃣ Dominio: PACIENTES

### 1.1 patient.registered
**Publicador:** `ms-gestionpacientes`  
**Trigger:** POST /pacientes exitoso  
**Subscribers:** ms-optimizacion, ms-notificaciones, AuditService

```json
{
  "eventType": "patient.registered",
  "eventId": "evt-uuid-123",
  "timestamp": "2026-05-27T10:35:00Z",
  "patientId": 123,
  "nombre": "Juan Pérez García",
  "apellido": "Pérez",
  "dni": "12345678A",
  "email": "juan@example.com",
  "telefono": "+56912345678",
  "edad": 45,
  "genero": "M",
  "especialidad_requerida": "Cardiología",
  "urgencia": "normal",
  "createdBy": "API"
}
```

**Acciones Desencadenadas:**
- ✅ ms-optimizacion: crea cita automáticamente
- ✅ ms-notificaciones: envía email de bienvenida
- ✅ AuditService: registra en audit_log

---

### 1.2 patient.updated
**Publicador:** `ms-gestionpacientes`  
**Trigger:** PUT /pacientes/{id} exitoso  
**Subscribers:** ms-optimizacion, ms-notificaciones, AuditService

```json
{
  "eventType": "patient.updated",
  "eventId": "evt-uuid-124",
  "timestamp": "2026-05-27T11:00:00Z",
  "patientId": 123,
  "changes": {
    "email": {
      "old": "juan@old.com",
      "new": "juan@new.com"
    },
    "telefono": {
      "old": "+56912345678",
      "new": "+56998765432"
    }
  },
  "updatedBy": "usuario-456"
}
```

**Acciones Desencadenadas:**
- ✅ ms-notificaciones: notifica cambios de contacto
- ✅ AuditService: registra cambios para compliance

---

### 1.3 patient.deleted
**Publicador:** `ms-gestionpacientes`  
**Trigger:** DELETE /pacientes/{id} exitoso (soft delete)  
**Subscribers:** ms-optimizacion, ms-notificaciones, AuditService

```json
{
  "eventType": "patient.deleted",
  "eventId": "evt-uuid-125",
  "timestamp": "2026-05-27T12:00:00Z",
  "patientId": 123,
  "reason": "SOLICITUD_RGPD_DERECHO_AL_OLVIDO",
  "deletedBy": "usuario-456"
}
```

**Acciones Desencadenadas:**
- ✅ ms-optimizacion: cancela citas futuras
- ✅ ms-notificaciones: marca notificaciones como archivadas
- ✅ AuditService: registra eliminación (RGPD compliant)

---

### 1.4 patient.on_waitlist
**Publicador:** `ms-gestionpacientes`  
**Trigger:** POST /lista-espera/{patientId} exitoso  
**Subscribers:** ms-optimizacion, AuditService

```json
{
  "eventType": "patient.on_waitlist",
  "eventId": "evt-uuid-126",
  "timestamp": "2026-05-27T10:40:00Z",
  "patientId": 123,
  "especialidad": "Cardiología",
  "urgencia": "prioritario",
  "estimatedWaitDays": 3,
  "addedBy": "recepcionista-789"
}
```

**Acciones Desencadenadas:**
- ✅ ms-optimizacion: reconsidera asignación de citas
- ✅ AuditService: registra entrada en lista

---

## 2️⃣ Dominio: CITAS

### 2.1 appointment.assigned
**Publicador:** `ms-optimizacion`  
**Trigger:** Automático al registrar paciente O POST /optimizacion/asignar  
**Subscribers:** ms-notificaciones, AuditService

```json
{
  "eventType": "appointment.assigned",
  "eventId": "evt-uuid-200",
  "timestamp": "2026-05-27T10:45:00Z",
  "appointmentId": 456,
  "patientId": 123,
  "medicoId": 789,
  "medicoNombre": "Dr. López García",
  "especialidad": "Cardiología",
  "fecha": "2026-05-29",
  "hora": "09:00",
  "sala": "Consultorio 3",
  "estrategia_usada": "FIFO",
  "assignedBy": "ms-optimizacion"
}
```

**Acciones Desencadenadas:**
- ✅ ms-notificaciones: envía SMS + Email de cita asignada
- ✅ AuditService: registra asignación

---

### 2.2 appointment.rescheduled
**Publicador:** `ms-optimizacion`  
**Trigger:** PUT /optimizacion/citas/{id}/reasignar exitoso  
**Subscribers:** ms-notificaciones, AuditService

```json
{
  "eventType": "appointment.rescheduled",
  "eventId": "evt-uuid-201",
  "timestamp": "2026-05-27T14:00:00Z",
  "appointmentId": 456,
  "patientId": 123,
  "changes": {
    "fecha": {
      "old": "2026-05-29",
      "new": "2026-05-31"
    },
    "hora": {
      "old": "09:00",
      "new": "10:30"
    },
    "medico": {
      "old": "Dr. López",
      "new": "Dra. García"
    }
  },
  "razon": "SOLICITUD_PACIENTE",
  "rescheduledBy": "recepcionista-789"
}
```

**Acciones Desencadenadas:**
- ✅ ms-notificaciones: envía SMS + Email con nueva fecha
- ✅ AuditService: registra reasignación

---

### 2.3 appointment.cancelled
**Publicador:** `ms-optimizacion`  
**Trigger:** DELETE /optimizacion/citas/{id} exitoso  
**Subscribers:** ms-gestionpacientes, ms-notificaciones, AuditService

```json
{
  "eventType": "appointment.cancelled",
  "eventId": "evt-uuid-202",
  "timestamp": "2026-05-27T15:00:00Z",
  "appointmentId": 456,
  "patientId": 123,
  "razon": "NO_SHOW",
  "observaciones": "Paciente no asistió sin justificación",
  "cancelledBy": "sistema"
}
```

**Acciones Desencadenadas:**
- ✅ ms-gestionpacientes: actualiza estado lista espera
- ✅ ms-notificaciones: notifica cancelación
- ✅ AuditService: registra cancelación

---

### 2.4 appointment.assignment_failed
**Publicador:** `ms-optimizacion`  
**Trigger:** Error al intentar asignar cita (no hay horarios disponibles)  
**Subscribers:** ms-notificaciones, AuditService

```json
{
  "eventType": "appointment.assignment_failed",
  "eventId": "evt-uuid-203",
  "timestamp": "2026-05-27T10:50:00Z",
  "patientId": 123,
  "especialidad": "Cardiología",
  "razon": "NO_HORARIOS_DISPONIBLES",
  "proximoDiaDisponible": "2026-06-15",
  "detalles": "Todos los médicos de Cardiología están ocupados hasta el 15 de junio"
}
```

**Acciones Desencadenadas:**
- ✅ ms-notificaciones: notifica al paciente que está en lista de espera
- ✅ AuditService: registra fallo para análisis

---

## 3️⃣ Dominio: NOTIFICACIONES

### 3.1 notification.created
**Publicador:** `ms-notificaciones`  
**Trigger:** POST /api/notifications exitoso O creación automática por eventos  
**Subscribers:** AuditService

```json
{
  "eventType": "notification.created",
  "eventId": "evt-uuid-300",
  "timestamp": "2026-05-27T10:50:00Z",
  "notificationId": 999,
  "patientId": 123,
  "tipoNotificacion": "CITA_ASIGNADA",
  "canal": "SMS",
  "destinatario": "+56912345678",
  "asunto": "Cita Médica Asignada",
  "cuerpo": "Su cita con Dr. López está confirmada para el 29/05 a las 09:00",
  "estado": "PENDIENTE",
  "scheduledFor": "2026-05-27T10:55:00Z",
  "createdBy": "ms-notificaciones"
}
```

**Acciones Desencadenadas:**
- ✅ AuditService: registra creación para compliance

---

### 3.2 notification.sent
**Publicador:** `ms-notificaciones`  
**Trigger:** Notificación enviada exitosamente (Email, SMS, Push)  
**Subscribers:** AuditService

```json
{
  "eventType": "notification.sent",
  "eventId": "evt-uuid-301",
  "timestamp": "2026-05-27T10:55:30Z",
  "notificationId": 999,
  "patientId": 123,
  "canal": "SMS",
  "destinatario": "+56912345678",
  "proveedor": "Twilio",
  "providerMessageId": "SM1234567890abcdef",
  "estado": "ENVIADA",
  "tiempoEnvio_ms": 150
}
```

**Acciones Desencadenadas:**
- ✅ AuditService: registra envío exitoso

---

### 3.3 notification.failed
**Publicador:** `ms-notificaciones`  
**Trigger:** Fallo al intentar enviar notificación  
**Subscribers:** AuditService, Dead Letter Queue

```json
{
  "eventType": "notification.failed",
  "eventId": "evt-uuid-302",
  "timestamp": "2026-05-27T10:55:00Z",
  "notificationId": 999,
  "patientId": 123,
  "canal": "SMS",
  "destinatario": "+56912345678",
  "intentoNumero": 1,
  "errorCode": "INVALID_PHONE_NUMBER",
  "errorMessage": "Número de teléfono inválido",
  "proximoIntento": "2026-05-27T11:00:00Z"
}
```

**Acciones Desencadenadas:**
- ✅ AuditService: registra fallo
- ✅ Kafka DLQ: reintentará automáticamente (exponential backoff)
- ✅ Después de 3 intentos → manual review needed

---

## 4️⃣ Dominio: AUDITORÍA (Eventos del Sistema)

### 4.1 audit.event.created
**Publicador:** `AuditService`  
**Trigger:** Cualquier evento importante  
**Subscribers:** Logging centralizado, Compliance reports

```json
{
  "eventType": "audit.event.created",
  "eventId": "evt-uuid-400",
  "timestamp": "2026-05-27T10:35:00Z",
  "resourceType": "PATIENT",
  "resourceId": 123,
  "action": "CREATE",
  "actor": {
    "actorType": "API",
    "actorId": "frontend-v1"
  },
  "changes": {
    "nombre": "Juan Pérez",
    "email": "juan@example.com"
  },
  "ipAddress": "192.168.1.100",
  "userAgent": "Mozilla/5.0...",
  "status": "SUCCESS"
}
```

---

### 4.2 audit.access.logged
**Publicador:** `AuditService`  
**Trigger:** Acceso a datos sensibles (READ)  
**Subscribers:** Security monitoring, Compliance reports

```json
{
  "eventType": "audit.access.logged",
  "eventId": "evt-uuid-401",
  "timestamp": "2026-05-27T10:35:00Z",
  "resourceType": "PATIENT",
  "resourceId": 123,
  "accessType": "READ",
  "actor": {
    "userId": "medico-456",
    "role": "DOCTOR"
  },
  "ipAddress": "192.168.1.50",
  "purpose": "CONSULTATION",
  "dataFieldsAccessed": ["nombre", "dni", "email", "telefono"]
}
```

---

## 📊 Matriz de Suscriptores

| Evento | Publicador | Suscriptor 1 | Suscriptor 2 | Suscriptor 3 |
|--------|-----------|-------------|-------------|-------------|
| `patient.registered` | ms-gestionpacientes | ms-optimizacion | ms-notificaciones | AuditService |
| `patient.updated` | ms-gestionpacientes | ms-notificaciones | AuditService | - |
| `patient.deleted` | ms-gestionpacientes | ms-optimizacion | ms-notificaciones | AuditService |
| `patient.on_waitlist` | ms-gestionpacientes | ms-optimizacion | AuditService | - |
| `appointment.assigned` | ms-optimizacion | ms-notificaciones | AuditService | - |
| `appointment.rescheduled` | ms-optimizacion | ms-notificaciones | AuditService | - |
| `appointment.cancelled` | ms-optimizacion | ms-gestionpacientes | ms-notificaciones | AuditService |
| `appointment.assignment_failed` | ms-optimizacion | ms-notificaciones | AuditService | - |
| `notification.created` | ms-notificaciones | AuditService | - | - |
| `notification.sent` | ms-notificaciones | AuditService | - | - |
| `notification.failed` | ms-notificaciones | AuditService | (DLQ) | - |
| `audit.event.created` | AuditService | Logging | Compliance | - |
| `audit.access.logged` | AuditService | Security | Compliance | - |

---

## 🔄 Secuencia Completa: Registrar Paciente

```
T+0ms:   [Cliente]
         POST /api/pacientes
         {"dni":"12345678A", "nombre":"Juan", ...}
         
T+5ms:   [API Gateway]
         Forward a ms-gestionpacientes
         
T+10ms:  [ms-gestionpacientes]
         ├─ Valida DNI no existe
         ├─ Crea Paciente entity
         ├─ Guarda en PostgreSQL
         ├─ EMITE: patient.registered
         └─ RETORNA: 201 Created
         
T+15ms:  [Al cliente]
         UI muestra: "Paciente registrado"
         
T+20ms:  [Kafka Topic: patient.registered]
         Tres suscriptores reciben:
         
T+25ms:  [ms-optimizacion]
         ├─ Recibe patient.registered
         ├─ Consulta lista_espera (Feign GET)
         ├─ Busca horario disponible
         ├─ Crea Cita en BD
         ├─ EMITE: appointment.assigned
         
T+25ms:  [ms-notificaciones]
         ├─ Recibe patient.registered
         ├─ Crea Notificacion "BIENVENIDA"
         ├─ Schedule para 5min después
         ├─ Guarda en BD
         
T+25ms:  [AuditService]
         ├─ Recibe patient.registered
         ├─ Crea audit_event
         ├─ Guarda en BD
         
T+30ms:  [Kafka Topic: appointment.assigned]
         ms-notificaciones recibe:
         
T+35ms:  [ms-notificaciones - 2do evento]
         ├─ Recibe appointment.assigned
         ├─ Crea Notificacion "CITA_ASIGNADA"
         ├─ ENVÍA INMEDIATAMENTE (SMS + EMAIL)
         ├─ Guarda comprobante
         ├─ EMITE: notification.sent
         
T+50ms:  [AuditService]
         ├─ Recibe appointment.assigned
         ├─ Registra asignación
         
T+60ms:  [AuditService]
         ├─ Recibe notification.sent
         ├─ Registra envío

RESULTADO (200ms después):
✅ Paciente registrado
✅ Cita asignada automáticamente
✅ 2 notificaciones enviadas
✅ Auditoría completa en BD
```

---

## 🛑 Manejo de Errores Por Dominio

### Si ms-optimizacion no puede asignar cita:

```
client.assigned_failed
  ↓
ms-notificaciones recibe
  ├─ Crea notificación: "En lista de espera"
  ├─ Envía SMS
  └─ EMITE: notification.sent
  
RESULTADO:
- Paciente registrado ✅
- Sin cita ahora, pero en lista ✅
- Notificado de su estado ✅
- Sin rollback ✅ (transacción parcial OK)
```

### Si ms-notificaciones falla al enviar SMS:

```
notification.failed
  ↓
Kafka DLQ retiene mensaje
  ↓
Retry automático con exponential backoff:
T+0s: Intento 1
T+5s: Intento 2
T+15s: Intento 3
T+60s: Manual review needed
  
RESULTADO:
- Paciente registrado ✅
- Cita asignada ✅
- SMS probará 3 veces ✅
- Si falla → Admin revisará ✅
- Sin rollback ✅ (notificación es non-critical)
```

---

## 📝 Payload Estándar (Todos los Eventos)

```json
{
  "eventType": "tipo.del.evento",
  "eventId": "evt-uuid-uniqueid",
  "timestamp": "ISO8601",
  "version": 1,
  "sourceService": "ms-xxx",
  // ... datos específicos del evento
}
```

---

## 🔐 Consideraciones de Seguridad

- **PII Masking:** No incluir datos sensibles (SSN, números tarjeta) en eventos
- **Encryption:** Mensajes Kafka con TLS
- **RBAC:** Solo servicios autorizados pueden suscribirse
- **Audit Trail:** Todos los eventos registrados para compliance

---

## 📚 Dónde Publicar/Escuchar Cada Evento

**En ms-gestionpacientes:**
```java
@Component
public class PacienteEventPublisher {
    @Autowired
    private KafkaTemplate kafkaTemplate;
    
    public void publishPatientRegistered(PatientRegisteredEvent e) {
        kafkaTemplate.send("patient.registered", objectMapper.writeValueAsString(e));
    }
}
```

**En ms-optimizacion:**
```java
@Component
public class PatientEventListener {
    @KafkaListener(topics = "patient.registered", groupId = "optimizacion-group")
    public void onPatientRegistered(String message) {
        PatientRegisteredEvent event = objectMapper.readValue(message, ...);
        // Asignar cita...
    }
}
```

**En ms-notificaciones:**
```java
@Component
public class NotificacionEventListeners {
    @KafkaListener(topics = "patient.registered", groupId = "notificaciones-group")
    public void onPatientRegistered(String message) { ... }
    
    @KafkaListener(topics = "appointment.assigned", groupId = "notificaciones-group")
    public void onAppointmentAssigned(String message) { ... }
}
```

---

## ✅ Checklist: Implementación de Eventos

- [ ] Crear clase Event DTO (PatientRegisteredEvent, etc.)
- [ ] Crear publisher en servicio origen
- [ ] Crear listener en servicios suscriptores
- [ ] Configurar Kafka topic
- [ ] Implementar serialización JSON
- [ ] Testing unitario de publishers
- [ ] Testing unitario de listeners
- [ ] Testing integración (publisher → listener)
- [ ] Documentar evento en este catálogo

---

**Documento:** EVENTOS.md  
**Versión:** 1.0  
**Última Actualización:** 27 de Mayo, 2026  
**Estado:** Proposal

