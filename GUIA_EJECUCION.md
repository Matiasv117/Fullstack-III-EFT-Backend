# 🚀 GUÍA DE EJECUCIÓN - RedNorte Portal

## Requisitos Previos

```bash
✅ Java 17+
✅ Maven 3.8+
✅ Node.js 16+ y npm
✅ PostgreSQL 12+ (si usas base de datos)
✅ Git
```

---

## 1️⃣ INICIAR EUREKA SERVER

```bash
# Terminal 1: Eureka Server
cd C:\Users\ibane\Desktop\Fullstack\eureka-server
mvn clean spring-boot:run
```

**Verificar en navegador:**
```
http://localhost:8761
```

---

## 2️⃣ INICIAR MICROSERVICIOS

### MS Gestión de Pacientes

```bash
# Terminal 2
cd C:\Users\ibane\Desktop\Fullstack\ms-gestionpacientes
mvn clean spring-boot:run
```

**Puerto:** 8081
**Health Check:** `http://localhost:8081/actuator/health`

### MS Optimización

```bash
# Terminal 3
cd C:\Users\ibane\Desktop\Fullstack\ms-optimizacion
mvn clean spring-boot:run
```

**Puerto:** 8082
**Health Check:** `http://localhost:8082/actuator/health`

### MS Notificaciones (Opcional)

```bash
# Terminal 4
cd C:\Users\ibane\Desktop\Fullstack\ms-notificaciones
mvn clean spring-boot:run
```

**Puerto:** 8083

### BFF (Backend for Frontend)

```bash
# Terminal 5
cd C:\Users\ibane\Desktop\Fullstack\bff
mvn clean spring-boot:run
```

**Puerto:** 8080
**Documentación:** `http://localhost:8080/swagger-ui.html`

---

## 3️⃣ INICIAR FRONTEND

```bash
# Terminal 6
cd C:\Users\ibane\Desktop\Fullstack\Fullstack-III-EFT-Frontend
npm install
npm run dev
```

**URL:** `http://localhost:5173`

---

## ✅ VERIFICACIÓN DE SERVICIOS

```bash
# Eureka Server
curl http://localhost:8761

# BFF Health
curl http://localhost:8080/actuator/health

# Pacientes API
curl http://localhost:8081/pacientes

# Optimización API
curl http://localhost:8082/optimizacion/lista-espera

# Frontend
http://localhost:5173
```

---

## 🎯 PRUEBA RÁPIDA EN ORDEN

### 1. Crear Paciente

**Frontend:** Ir a pestaña "Pacientes"
```
Nombre: Juan
Apellido: Pérez
DNI: 12345678
Click: "Registrar paciente"
```

**Resultado esperado:** Mensaje de éxito, paciente aparece en lista

### 2. Agregar a Lista de Espera

**Frontend:** Click en "Agregar a lista" del paciente creado
```
Result esperado: Mensaje "Paciente agregado a lista de espera"
```

### 3. Ver Lista de Espera

**Frontend:** Ir a pestaña "Lista de Espera"
```
Resultado esperado: Paciente aparece con estado PENDIENTE
```

### 4. Cambiar Estado

**Frontend:** En "Lista de Espera", selector "Cambiar estado" → ATENDIDO
```
Resultado esperado: Badge de estado cambia a verde "ATENDIDO"
```

### 5. Simular Cancelación de Cita

**Frontend:** Ir a "Optimización"
```
ID de Cita: 1
Estrategia: FIFO
Click: "Procesar Cancelación"
Resultado esperado: Alerta confirmando reasignación
```

---

## 🐛 SOLUCIÓN DE PROBLEMAS

### "No se puede conectar a localhost:8081"

```bash
# Verificar que ms-gestionpacientes esté corriendo
# Terminal específica debe mostrar: "Started Application in X seconds"
# Si no, ejecutar:
mvn clean install -DskipTests
mvn spring-boot:run
```

### "CORS Error" en frontend

```bash
# Asegurar que BFF esté corriendo
# BFF es el que maneja los CORS
# Verificar puerto 8080

curl -i http://localhost:8080/actuator/health
```

### "Pacientes no aparecen en lista"

```bash
# 1. Verificar Eureka: http://localhost:8761
#    Debe mostrar 3-4 servicios registrados

# 2. Revisar consola del navegador (F12)
#    Verificar errores de red

# 3. Probar directamente la API:
curl http://localhost:8081/pacientes
curl http://localhost:8080/pacientes  # A través del BFF
```

### "Base de datos vacía"

```bash
# Si usas PostgreSQL, correr migraciones:
# Las migraciones están en: migrations/

# Si usas H2 (por defecto), los datos se pierden al reiniciar
# Es comportamiento esperado en desarrollo
```

---

## 📊 DASHBOARDS Y DOCUMENTACIÓN

```
Eureka Dashboard:      http://localhost:8761
Swagger API Docs:      http://localhost:8080/swagger-ui.html
Frontend:              http://localhost:5173
Frontend Vite Logs:    Terminal - npm run dev
```

---

## 🔧 DESARROLLO Y DEBUGGING

### Activar logs detallados

**File:** `application.properties` en cada microservicio

```properties
logging.level.root=INFO
logging.level.com.saludrednorte=DEBUG
logging.level.org.springframework.web=DEBUG
```

### Hot Reload en Frontend

```bash
# Ya está habilitado con Vite
# Cambios en archivos se recargan automáticamente
```

### Hot Reload en Backend (Opcional)

```xml
<!-- En pom.xml de cada servicio -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
</dependency>
```

---

## 📱 TESTING ENDPOINTS CON CURL/POSTMAN

### Crear Paciente

```bash
curl -X POST http://localhost:8080/pacientes \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Juan",
    "apellido": "Pérez",
    "dni": "12345678",
    "telefono": "555-1234",
    "email": "juan@example.com"
  }'
```

### Obtener Pacientes

```bash
curl http://localhost:8080/pacientes
```

### Agregar a Lista de Espera

```bash
curl -X POST http://localhost:8080/lista-espera \
  -H "Content-Type: application/json" \
  -d '{
    "paciente": { "id": 1 },
    "gravedad": "MEDIA",
    "interconsulta": "Cardiología",
    "estado": "PENDIENTE"
  }'
```

### Obtener Lista de Espera

```bash
curl http://localhost:8080/lista-espera
```

### Cambiar Estado

```bash
curl -X PUT http://localhost:8080/lista-espera/1/estado/ATENDIDO
```

### Eliminar de Lista

```bash
curl -X DELETE http://localhost:8080/lista-espera/1
```

### Optimización: Listar

```bash
curl http://localhost:8080/optimizacion/lista-espera
```

### Optimización: Cancelar

```bash
curl -X POST "http://localhost:8080/optimizacion/cancelar/1?estrategia=fifo"
```

---

## 🛑 PARAR SERVICIOS

```bash
# En cada terminal ejecutando servicios:
Ctrl + C

# Matar procesos específicos (si es necesario):
# Windows:
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Linux/Mac:
lsof -i :8080
kill -9 <PID>
```

---

## 📈 MONITOREO

### Ver logs en tiempo real

```bash
# En cada terminal de servicio, mirar las salidas

# O redirigir a archivo:
mvn spring-boot:run > service.log 2>&1
```

### Verificar puertos en uso

```bash
# Windows:
netstat -ano | findstr ESTABLISHED

# Linux/Mac:
lsof -i -P -n
```

---

## 🎓 ARQUITECTURA DE PUERTOS

```
┌─────────────────────────────────────────────────────┐
│                   CLIENTE (5173)                    │ ← Frontend React/Vite
└──────────────────────┬──────────────────────────────┘
                       │ HTTP
┌──────────────────────▼──────────────────────────────┐
│              BFF (8080)                             │ ← Backend for Frontend
│          - CORS habilitado                          │
│          - Enrutamiento de requests                 │
└┬───────────────────┬────────────────────────┬───────┘
 │                   │                        │
 │                   │                        │
 ▼                   ▼                        ▼
(8081)         (8082)                    (8083)
Gestión        Optimización              Notificaciones
Pacientes

     ▲              ▲                        ▲
     └──────────────┼────────────────────────┘
                    │ Discovery
                    ▼
            Eureka Server (8761)
            - Service Registry
            - Health Checks
```

---

## 🔐 VARIABLES DE ENTORNO (Si aplica)

```bash
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=rednorte
DB_USER=postgres
DB_PASSWORD=yourpassword

# Notificaciones
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USER=your-email@gmail.com
MAIL_PASSWORD=your-app-password

# API
API_BASE_URL=http://localhost:8080
FRONTEND_URL=http://localhost:5173
```

---

## ✨ FLUJO TÍPICO DE DESARROLLO

```
1. Iniciar Eureka (Terminal 1)
2. Iniciar Microservicios (Terminales 2-5)
3. Iniciar Frontend (Terminal 6)
4. Abrir navegador: http://localhost:5173
5. Hacer cambios en código
6. Frontend hot-reloads automáticamente
7. Backend: reiniciar servicio afectado si es necesario
8. Revisar Eureka si hay problemas de conectividad
```

---

## 📚 DOCUMENTACIÓN ÚTIL

- **API Docs:** http://localhost:8080/swagger-ui.html
- **Eureka:** http://localhost:8761
- **Frontend Logs:** Consola de navegador (F12)
- **Backend Logs:** Consola de cada terminal Maven

---

## ✅ CHECKLIST PRE-DEMO

- [ ] Todos los servicios están corriendo (verificar Eureka)
- [ ] Frontend abre en http://localhost:5173
- [ ] Puedo crear un paciente exitosamente
- [ ] Paciente aparece en la lista
- [ ] Puedo agregar a lista de espera
- [ ] Puedo ver lista de espera
- [ ] Puedo cambiar estado
- [ ] Filtros funcionan correctamente
- [ ] Puedo eliminar pacientes y registros
- [ ] Consola del navegador sin errores en rojo

---

**Fecha:** 2026-05-11
**Versión:** 1.0
**Estado:** LISTO PARA EJECUCIÓN ✅

