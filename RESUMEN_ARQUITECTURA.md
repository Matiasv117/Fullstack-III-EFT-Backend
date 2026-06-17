# Resumen de Arquitectura - Backend Sistema de Salud Red Norte

## Visión General
Sistema de gestión de pacientes y citas médicas implementado con arquitectura de microservicios y Backend For Frontend (BFF).

## Microservicios

### 1. ms-gestionpacientes (ms-listas-espera)
**Responsabilidades:**
- Gestión de pacientes (CRUD completo)
- Gestión de lista de espera
- Integración con ms-optimizacion para creación automática de citas
- Envío de notificaciones a ms-notificaciones

**Tecnologías:**
- Spring Boot 3.4.1
- Spring Cloud 2024.0.0
- Spring Data JPA
- PostgreSQL / H2
- Spring Cloud OpenFeign
- SpringDoc OpenAPI 2.3.0

**Endpoints principales:**
- `POST /pacientes` - Registrar paciente (crea cita automáticamente)
- `GET /pacientes` - Obtener todos los pacientes
- `GET /pacientes/{id}` - Obtener paciente por ID
- `PUT /pacientes` - Actualizar paciente
- `DELETE /pacientes/{id}` - Eliminar paciente
- `POST /lista-espera` - Agregar a lista de espera
- `GET /lista-espera` - Obtener lista de espera
- `GET /lista-espera/estado/{estado}` - Filtrar por estado
- `GET /lista-espera/gravedad/{gravedad}` - Filtrar por gravedad

**Integración:**
- `CitaClient` - Cliente Feign para comunicarse con ms-optimizacion
- `NotificationClient` - Cliente Feign para enviar notificaciones

### 2. ms-optimizacion
**Responsabilidades:**
- Gestión de citas médicas
- Gestión de médicos
- Gestión de horarios médicos
- Optimización y reasignación automática de citas
- Cálculo de prioridad de pacientes

**Tecnologías:**
- Spring Boot 3.4.1
- Spring Cloud 2024.0.0
- Spring Data JPA
- PostgreSQL / H2
- Spring Cloud OpenFeign
- Resilience4j (Circuit Breaker)
- SpringDoc OpenAPI 2.3.0

**Endpoints principales:**
- `POST /citas` - Crear cita
- `GET /citas` - Obtener todas las citas
- `GET /citas/estado/{estado}` - Filtrar por estado
- `GET /citas/{id}` - Obtener cita por ID
- `PUT /citas` - Actualizar cita
- `DELETE /citas/{id}` - Cancelar cita
- `POST /medicos` - Registrar médico
- `GET /medicos` - Obtener todos los médicos
- `POST /horarios` - Crear horario
- `GET /horarios/disponibles` - Obtener horarios disponibles
- `POST /optimizacion/cancelar/{citaId}` - Procesar cancelación con reasignación
- `GET /optimizacion/prioridad` - Calcular prioridad de paciente

**Integración:**
- `ListaEsperaClient` - Cliente Feign para obtener lista de espera
- `NotificationClient` - Cliente Feign para enviar notificaciones

## DTOs y Entidades

### ms-gestionpacientes
**DTOs:**
- `CitaDTO` - Transferencia de datos de citas
- `MedicoDTO` - Transferencia de datos de médicos
- `NotificationRequestDTO` - Solicitud de notificación

**Entidades:**
- `Paciente` - Información del paciente
- `ListaEspera` - Pacientes en lista de espera

### ms-optimizacion
**DTOs:**
- `ListaEsperaDTO` - Transferencia de datos de lista de espera
- `PrioridadResponse` - Respuesta de cálculo de prioridad

**Entidades:**
- `Cita` - Información de la cita médica
- `Medico` - Información del médico
- `Horario` - Horarios médicos disponibles

## Integración Inter-Servicios

### Flujo de Registro de Paciente
1. Cliente registra paciente en `ms-gestionpacientes`
2. `PacienteService` guarda paciente en BD
3. `CitaClient` obtiene médicos disponibles de `ms-optimizacion`
4. `CitaClient` crea cita automáticamente con el primer médico disponible
5. `NotificationClient` envía notificación a `ms-notificaciones`

### Flujo de Cancelación de Cita
1. Cliente cancela cita en `ms-optimizacion`
2. `OptimizacionService` procesa cancelación
3. `ListaEsperaClient` obtiene lista de espera de `ms-gestionpacientes`
4. Estrategia de reasignación (FIFO o Prioridad) selecciona candidato
5. Cita se reasigna al paciente seleccionado
6. `NotificationClient` envía notificación de reasignación

## Estrategias de Optimización

### Estrategia FIFO
- Reasigna citas basándose en el orden de llegada
- Paciente con menor ID en lista de espera tiene prioridad

### Estrategia de Prioridad
- Calcula prioridad basada en:
  - Gravedad (1-5)
  - Distancia geográfica (km)
  - Días acumulados en espera
- Reasigna al paciente con mayor prioridad

## Documentación API

### Swagger/OpenAPI
- **ms-gestionpacientes**: `http://localhost:8080/swagger-ui.html`
- **ms-optimizacion**: `http://localhost:8081/swagger-ui.html`

### Endpoints Documentados
- Todos los endpoints REST están documentados con anotaciones Swagger
- Incluye descripciones, parámetros y códigos de respuesta
- Organizados por tags (Pacientes, Lista de Espera, Citas, Médicos, etc.)

## Tests

### Cobertura de Tests
- **ms-gestionpacientes**: 68 tests pasando
- **ms-optimizacion**: 95 tests pasando

### Configuración de Tests
- JUnit 5
- Mockito para mocking
- Tests unitarios de servicios y controladores
- Integración de clientes Feign mockeados

## Configuración de Java 25

### Problemas Resueltos
- **JaCoCo**: Deshabilitado temporalmente por incompatibilidad con Java 25
- **Byte Buddy**: Propiedad `-Dnet.bytebuddy.experimental=true` agregada para soporte experimental de Java 25

## Arquitectura Futura (Propuesta)

### Event-Driven con Kafka
- Migración de comunicación síncrona a asíncrona
- Uso de Kafka para eventos de dominio
- Implementación de SAGA Pattern para transacciones distribuidas

### Mejoras Planificadas
- Implementación completa de Event-Driven Architecture
- Circuit Breaker mejorado con Resilience4j
- Observabilidad con Micrometer y Prometheus
- Logging estructurado con ELK Stack

## Resumen de Cambios Recientes

1. **CitaClient creado** en ms-gestionpacientes
   - Cliente Feign para comunicación con ms-optimizacion
   - Métodos para gestión de citas y médicos

2. **Integración automática de citas**
   - Al registrar paciente, se crea cita automáticamente
   - Se asigna el primer médico disponible
   - Fecha programada para el día siguiente

3. **Documentación Swagger/OpenAPI**
   - Configuración en ambos microservicios
   - Anotaciones en todos los controladores
   - Documentación completa de endpoints

4. **Tests actualizados**
   - PacienteServiceTest con mock de CitaClient
   - EstrategiaFIFOTest con mocks de dependencias
   - Todos los tests pasando exitosamente

## Estado del Proyecto

✅ **Backend funcional y listo para defensa**
- Todos los microservicios compilando correctamente
- Tests unitarios pasando (163 tests totales)
- API REST documentada con Swagger
- Integración inter-servicios implementada
- Arquitectura alineada con requisitos del parcial
