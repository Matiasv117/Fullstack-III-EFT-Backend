# ✅ CHECKLIST EJECUTABLE - Paso a Paso

**Versión:** Final  
**Estado:** Listo para comenzar  
**Duración:** 6-7 horas  

---

## 🚀 COMIENZA AQUÍ

Copía este checklist en un documento y marca cada paso mientras avanzas.

---

## FASE 1: INTEGRACIÓN (2-3 HORAS)

### PASO 1.1: Conectar ms-gestionpacientes → ms-notificaciones

**Archivo:** `ms-gestionpacientes/src/main/java/com/saludrednorte/ms_listas_espera/service/PacienteService.java`

```
☐ Abre el archivo en tu IDE
☐ Busca la línea "private PacienteRepository pacienteRepository;"
☐ Agrega debajo: "@Autowired private NotificationClient notificationClient;"
☐ En método registrarPaciente():
  ☐ Busca línea "return pacienteRepository.save(paciente);"
  ☐ Antes de return, agrega:
    ```java
    try {
        NotificationRequestDTO notif = new NotificationRequestDTO();
        notif.setPacienteId(savedPaciente.getId());
        notif.setTipo("PACIENTE_REGISTRADO");
        notif.setMensaje("Paciente " + savedPaciente.getNombre() + " registrado");
        notif.setCanal("EMAIL");
        notificationClient.createNotification(notif);
    } catch (Exception e) {
        logger.warn("Notificación falló pero paciente registrado", e);
    }
    ```
☐ En método actualizarPaciente():
  ☐ Antes de return, agrega código similar (tipo PACIENTE_ACTUALIZADO)
☐ Guarda el archivo
☐ En terminal: mvnw clean compile
☐ ✅ Verifica que compile sin errores
```

**Tiempo:** 30 minutos  
**Status:** ☐

---

### PASO 1.2: Conectar ms-optimizacion → ms-notificaciones

#### A) Crear NotificationClient
```
Archivo: ms-optimizacion/src/main/java/com/saludrednorte/ms_optimizacion/client/NotificationClient.java

☐ Crea archivo nuevo (o edita si existe)
☐ Copia este código:
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
☐ Guarda archivo
```

#### B) Crear NotificationRequestDTO
```
Archivo: ms-optimizacion/src/main/java/com/saludrednorte/ms_optimizacion/dto/NotificationRequestDTO.java

☐ Crea archivo nuevo
☐ Copia este código:
```java
package com.saludrednorte.ms_optimizacion.dto;

public class NotificationRequestDTO {
    private Long pacienteId;
    private String tipo;
    private String mensaje;
    private String canal;
    
    public NotificationRequestDTO() {}
    
    public Long getPacienteId() { return pacienteId; }
    public void setPacienteId(Long pacienteId) { this.pacienteId = pacienteId; }
    
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    
    public String getCanal() { return canal; }
    public void setCanal(String canal) { this.canal = canal; }
}
```
☐ Guarda archivo
```

#### C) Modificar OptimizacionService
```
Archivo: ms-optimizacion/src/main/java/com/saludrednorte/ms_optimizacion/service/OptimizacionService.java

☐ Abre archivo
☐ Busca "private ListaEsperaClient listaEsperaClient;"
☐ Agrega debajo: "@Autowired private NotificationClient notificationClient;"
☐ En método procesarCancelacion():
  ☐ Busca el final del método
  ☐ Agrega:
    ```java
    try {
        NotificationRequestDTO notif = new NotificationRequestDTO();
        notif.setPacienteId(citaReasignada.getPacienteId());
        notif.setTipo("CITA_REASIGNADA");
        notif.setMensaje("Cita reasignada");
        notif.setCanal("SMS");
        notificationClient.createNotification(notif);
    } catch (Exception e) {
        logger.warn("Notificación falló", e);
    }
    ```
☐ Guarda archivo
☐ En terminal: mvnw clean compile
☐ ✅ Verifica que compile sin errores
```

**Tiempo:** 45 minutos  
**Status:** ☐

---

### PASO 1.3: Verificar DTOs

```
☐ Verificar que exista: ms-gestionpacientes/.../dto/NotificationRequestDTO.java
☐ Verificar que exista: ms-optimizacion/.../dto/NotificationRequestDTO.java
☐ Ambos deben tener: pacienteId, tipo, mensaje, canal
☐ Si faltan, crea el archivo (copiar código de arriba)
```

**Tiempo:** 15 minutos  
**Status:** ☐

---

### PASO 1.4: Testing FASE 1

```
☐ En terminal (ms-gestionpacientes): mvnw clean install
  ☐ Espera a que termine (2-3 min)
  ☐ ✅ Debe terminar sin errores

☐ En terminal (ms-optimizacion): mvnw clean install
  ☐ Espera a que termine (2-3 min)
  ☐ ✅ Debe terminar sin errores

☐ En terminal (ms-notificaciones): mvnw clean install
  ☐ Espera a que termine (2-3 min)
  ☐ ✅ Debe terminar sin errores

SI TIENES ERRORES:
  ☐ Lee el mensaje de error
  ☐ Verifica imports
  ☐ Verifica nombres de clases
  ☐ Verifica paquetes
```

**Tiempo:** 15 minutos  
**Status:** ☐

---

## FASE 2: SINCRONIZAR SPRING BOOT (1-2 HORAS)

### PASO 2.1: Actualizar pom.xml de ms-notificaciones

```
Archivo: ms-notificaciones/pom.xml

☐ Abre el archivo
☐ Busca <version>2.7.12</version> (debe ser la línea 8-10)
☐ Cámbiala a: <version>4.0.4</version>
☐ Busca <java.version>11</java.version>
☐ Cámbiala a: <java.version>17</java.version>
☐ Busca <spring-cloud.version>2021.0.8</spring-cloud.version>
☐ Cámbiala a: <spring-cloud.version>2025.1.1</spring-cloud.version>
☐ Cámbiala a: <version>2.8.13</version>

☐ Guarda archivo
☐ En terminal: cd ms-notificaciones && mvnw clean install
☐ Espera 3-5 minutos (es normal que tarde)
☐ ✅ Debe terminar con "BUILD SUCCESS"

SI FALLA:
  ☐ Ejecuta: mvnw clean
  ☐ Intenta nuevamente
  ☐ Si persiste, verifica cambios exactos en pom.xml
```

**Tiempo:** 1-2 horas (incluye compilación)  
**Status:** ☐

---

## FASE 3: INSFORGE (2-3 HORAS)

### PASO 3.1: Obtener Insforge

```
☐ INVESTIGAR:
  ☐ Busca en Google: "Insforge"
  ☐ O pregunta al profesor por URL exacta
  ☐ Accede a la web
  ☐ Crea cuenta (Sign up)
  ☐ Verifica email
  ☐ Loguea

☐ CREAR PROYECTO:
  ☐ En dashboard → "New Project" o similar
  ☐ Nombre: "SaludRedNorte" (o similar)
  ☐ Click en "Create"

☐ OBTENER CREDENCIALES:
  ☐ Busca sección de "API Keys" o "Settings"
  ☐ Copia: API Key
  ☐ Copia: Project ID
  ☐ Anota: API URL (probablemente https://api.insforge.io/v1)

☐ GUARDAR EN .env:
  ☐ En raíz del proyecto, crea archivo: .env
  ☐ Contenido:
    ```env
    INSFORGE_API_KEY=tu_api_key_aqui
    INSFORGE_PROJECT_ID=tu_project_id_aqui
    INSFORGE_API_URL=https://api.insforge.io/v1
    ```
  ☐ NO compartir este archivo (agregar a .gitignore)
```

**Tiempo:** 30 minutos  
**Status:** ☐

---

### PASO 3.2: Crear modelos en Insforge

```
☐ EN INSFORGE DASHBOARD:
  ☐ Busca sección: "Data Models" o "Collections"
  ☐ Click en "Create Collection"
  
☐ CREAR "pacientes":
  ☐ Nombre: pacientes
  ☐ Agregar campos:
    ☐ id (type: integer, autoIncrement: true)
    ☐ nombre (type: string, required: true)
    ☐ apellido (type: string, required: true)
    ☐ dni (type: string, unique: true)
    ☐ email (type: string)
    ☐ createdAt (type: timestamp, default: now)
  ☐ Click Save / Create

☐ CREAR "notificaciones":
  ☐ Nombre: notificaciones
  ☐ Agregar campos:
    ☐ id (type: integer, autoIncrement: true)
    ☐ pacienteId (type: integer)
    ☐ tipo (type: string)
    ☐ mensaje (type: string)
    ☐ canal (type: string)
    ☐ estado (type: string)
    ☐ creadoAt (type: timestamp)
    ☐ enviadoAt (type: timestamp)

☐ CREAR "citas":
  ☐ Nombre: citas
  ☐ Agregar campos:
    ☐ id (type: integer, autoIncrement: true)
    ☐ pacienteId (type: integer)
    ☐ medicoId (type: integer)
    ☐ fecha (type: date)
    ☐ estado (type: string)
    ☐ createdAt (type: timestamp)
```

**Tiempo:** 45 minutos  
**Status:** ☐

---

### PASO 3.3: Crear InsforgeService en cada microservicio

```
☐ EN ms-gestionpacientes:
  ☐ Crea archivo: src/.../service/InsforgeService.java
  ☐ Contenido: [VER CÓDIGO EN GUIA_INSFORGE.md - Opción A]
  ☐ Actualiza application.yml:
    ```yaml
    insforge:
      api:
        url: ${INSFORGE_API_URL}
        key: ${INSFORGE_API_KEY}
    ```

☐ EN ms-notificaciones:
  ☐ Repite lo mismo (copiar InsforgeService)

☐ EN ms-optimizacion:
  ☐ Repite lo mismo (copiar InsforgeService)

☐ COMPILAR TODOS:
  ☐ mvnw clean install en cada carpeta
  ☐ ✅ Todos deben compilar sin errores
```

**Tiempo:** 1 hora  
**Status:** ☐

---

### PASO 3.4: Variables de entorno

```
☐ EN RAÍZ DEL PROYECTO, edita .env:
  ```env
  INSFORGE_API_URL=https://api.insforge.io/v1
  INSFORGE_API_KEY=tu_valor_aqui
  INSFORGE_PROJECT_ID=tu_valor_aqui
  ```

☐ ASEGÚRATE QUE .env ESTÉ EN .gitignore:
  ☐ Abre .gitignore
  ☐ Agrega línea: .env
  ☐ Guarda
```

**Tiempo:** 15 minutos  
**Status:** ☐

---

## FASE 4: TESTING E2E (1 HORA)

### PASO 4.1: Compilar todo

```
☐ En terminal, en cada carpeta ejecuta:

☐ eureka-server:     mvnw clean install
☐ api-gateway:       mvnw clean install
☐ ms-gestionpacientes: mvnw clean install
☐ ms-notificaciones: mvnw clean install
☐ ms-optimizacion:   mvnw clean install

TODO DEBE COMPILAR SIN ERRORES ✅
```

**Tiempo:** 10 minutos  
**Status:** ☐

---

### PASO 4.2: Levantar servicios

```
ABRE 5 TERMINALES (o tabs) DIFERENTES:

☐ TERMINAL 1 - Eureka:
  cd eureka-server
  mvnw spring-boot:run
  ✅ Verifica que diga "Tomcat started on port(s): 8761"

☐ TERMINAL 2 - API Gateway:
  cd api-gateway
  mvnw spring-boot:run
  ✅ Verifica que diga "Tomcat started on port(s): 8080"

☐ TERMINAL 3 - ms-gestionpacientes:
  cd ms-gestionpacientes
  mvnw spring-boot:run
  ✅ Verifica que diga "Tomcat started on port(s): 8083"

☐ TERMINAL 4 - ms-notificaciones:
  cd ms-notificaciones
  mvnw spring-boot:run
  ✅ Verifica que diga "Tomcat started on port(s): 8085"

☐ TERMINAL 5 - ms-optimizacion:
  cd ms-optimizacion
  mvnw spring-boot:run
  ✅ Verifica que diga "Tomcat started on port(s): 8084"

TODO DEBE ESTAR CORRIENDO SIN ERRORES ✅
```

**Tiempo:** 5 minutos  
**Status:** ☐

---

### PASO 4.3: Tests manuales

```
☐ ABRE POSTMAN O CURL:

☐ TEST 1 - Registrar paciente:
  POST http://localhost:8080/pacientes
  Body (JSON):
  {
    "nombre": "Juan",
    "apellido": "Pérez",
    "dni": "12345678",
    "email": "juan@example.com"
  }
  ✅ Debe retornar 200 con datos del paciente

☐ TEST 2 - Listar pacientes:
  GET http://localhost:8080/pacientes
  ✅ Debe retornar lista con al menos 1 paciente

☐ TEST 3 - Ver notificaciones:
  GET http://localhost:8080/api/notifications
  ✅ Debe haber al menos 1 notificación "PACIENTE_REGISTRADO"

☐ TEST 4 - Ver lista de espera:
  GET http://localhost:8080/lista-espera
  ✅ Debe retornar datos

TODOS LOS TESTS DEBEN PASAR ✅
```

**Tiempo:** 15 minutos  
**Status:** ☐

---

## FASE 5: DOCUMENTACIÓN (30 MINUTOS)

### PASO 5.1: Actualizar ARCHITECTURE.md

```
Archivo: ARCHITECTURE.md

☐ Abre el archivo
☐ Al final, agrega nueva sección:

## Flujo E2E Final

1. Paciente se registra en POST /pacientes
2. ms-gestionpacientes crea notificación automáticamente
3. Datos guardados en Insforge
4. ms-notificaciones recibe y registra
5. En el futuro, ms-optimizacion puede asignar cita
6. Paciente es notificado

## Tecnologías Utilizadas

- Spring Boot 4.0.4
- Java 17
- Eureka para service discovery
- OpenFeign para comunicación inter-servicios
- Insforge como BD persistente

☐ Guarda archivo
```

**Tiempo:** 30 minutos  
**Status:** ☐

---

## ✅ CHECKLIST FINAL

```
ANTES DE CONSIDERAR COMPLETADO:

☐ FASE 1 Completada
  ☐ PacienteService conecta con notificaciones
  ☐ OptimizacionService conecta con notificaciones
  ☐ DTOs creados en ambos servicios
  ☐ Todo compila

☐ FASE 2 Completada
  ☐ Spring Boot 4.0.4 en todos
  ☐ Java 17 en todos
  ☐ spring-cloud 2025.1.1 en todos
  ☐ Compilación exitosa

☐ FASE 3 Completada
  ☐ Cuenta Insforge creada
  ☐ Proyecto creado
  ☐ Modelos creados (pacientes, notificaciones, citas)
  ☐ InsforgeService en los 3 servicios
  ☐ application.yml actualizado
  ☐ .env con credenciales

☐ FASE 4 Completada
  ☐ Todo compila sin errores
  ☐ Todos los servicios levantados
  ☐ Registrar paciente funciona
  ☐ Notificación se crea automáticamente
  ☐ Datos en Insforge persisten

☐ FASE 5 Completada
  ☐ ARCHITECTURE.md actualizado
  ☐ Sistema documentado

RESULTADO FINAL: SISTEMA COMPLETAMENTE INTEGRADO ✅
```

---

## 🚨 SI TIENES PROBLEMAS

| Problema | Solución Rápida |
|----------|-----------------|
| Compilación falla | Verifica imports y nombres de clases |
| Eureka no arranca | Verifica puerto 8761 |
| Notificación no se crea | Verifica Eureka está corriendo |
| Insforge no responde | Verifica API Key en .env |
| Conexión rechazada | Verifica puertos no ocupados |

---

## 📞 CUANDO HAYAS TERMINADO

Dile a tu profesor:
- ✅ Los 3 microservicios están integrados
- ✅ Se comunican automáticamente
- ✅ Datos persistentes en Insforge
- ✅ Versiones consistentes (Spring Boot 4.0.4)
- ✅ Documentado y explicable

---

**¿Listo? ¡Comienza con PASO 1.1 ahora mismo!** 🚀

